package com.farmacia.demo.controllers;

import com.farmacia.demo.dtos.FarmaciaDTO;
import com.farmacia.demo.models.Farmacia;
import com.farmacia.demo.repository.FarmaciaRepository;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping(value = "/farmacia/")
public class FarmaciaController {

    @Autowired
    FarmaciaRepository farmaciaRepository;

    private Farmacia getFarmaciaLogada() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return farmaciaRepository.findByEmail(auth.getName()).get();
    }

    @GetMapping("/login")
    public String login() {
        return "farmacia/login";
    }

    @GetMapping("/cadastro")
    public String getCadastro() {
        return "farmacia/cadastro";
    }

    @PostMapping("/cadastro")
    public ModelAndView postCadastro(@Valid FarmaciaDTO farmaciaDTO, BindingResult result) {

        ModelAndView modelAndView = new ModelAndView("farmacia/cadastro");
        modelAndView.addObject("farmaciaDados", farmaciaDTO);

        // Verifica se existe algum erro nos campos informados pelo usuário.
        if (result.hasErrors()) {
            modelAndView.addObject("mensagem", "Verifique os campos!");
            return modelAndView;
        }

        // Verifica se as senhas correspondem.
        if (!farmaciaDTO.getSenha().equals(farmaciaDTO.getConfirmaSenha())) {
            modelAndView.addObject("mensagemSenha", "Senhas não correspondem");
            return modelAndView;
        }

        // Verifica se já existe um usuário cadastrado com o CNPJ informado.
        if (farmaciaRepository.existsById(farmaciaDTO.getCnpj())) {
            modelAndView.addObject("mensagem", "Já existe uma farmácia cadastrada com o CNPJ informado!");
            return modelAndView;
        }

        // Verifica se já existe um usuário cadastrado com o e-mail informado.
        if (farmaciaRepository.findByEmail(farmaciaDTO.getEmail()).isPresent()) {
            modelAndView.addObject("mensagem", "Já existe uma farmácia cadastrada com o e-mail informado!");
            return modelAndView;
        }

        try {

            Farmacia farmacia = new Farmacia();
            BeanUtils.copyProperties(farmaciaDTO, farmacia);
            farmacia.setRole("USER");
            farmacia.setSenha(new BCryptPasswordEncoder().encode(farmaciaDTO.getSenha()));
            farmaciaRepository.save(farmacia);

            modelAndView.addObject("mensagem", "Cadastro realizado com sucesso!");

        } catch (Exception e) {
            System.out.println(e);
            modelAndView.addObject("mensagem", "Erro ao realizar cadastro!");
        }

        return modelAndView;
    }

    @GetMapping("/conta")
    public ModelAndView getAtualizarDadosDaConta() {
        ModelAndView modelAndView = new ModelAndView("farmacia/dados_da_conta");
        modelAndView.addObject("farmaciaDados", getFarmaciaLogada());

        return modelAndView;
    }

    @PostMapping("/conta")
    public ModelAndView postAtualizarDadosDaConta(FarmaciaDTO farmaciaDTO) {

        ModelAndView modelAndView = new ModelAndView("farmacia/dados_da_conta");
        modelAndView.addObject("farmaciaDados", farmaciaDTO);

        // Verifica se existe algum erro nos campos informados pelo usuário.
        if (farmaciaDTO.getEmail().isEmpty() || farmaciaDTO.getNome().isEmpty()) {
            modelAndView.addObject("mensagem", "Verifique os campos!");
            return modelAndView;
        }

        // Verifica se já existe uma farmácia cadastrada cadastrada com o e-mail informado.
        if (farmaciaRepository.findByEmail(farmaciaDTO.getEmail()).isPresent() && !farmaciaDTO.getEmail().equals(getFarmaciaLogada().getEmail())) {
            modelAndView.addObject("mensagem", "Já existe uma farmácia cadastrada com o e-mail informado!");
            return modelAndView;
        }

        try {

            Farmacia farmacia = getFarmaciaLogada();
            farmacia.setEmail(farmaciaDTO.getEmail());
            farmacia.setNome(farmaciaDTO.getNome());
            farmaciaRepository.save(farmacia);
            modelAndView.addObject("farmaciaDados", farmacia);
            modelAndView.addObject("mensagem", "Atualização realizada com sucesso!");

        } catch (Exception e) {
            System.out.println(e);
            modelAndView.addObject("farmaciaDados", farmaciaDTO);
            modelAndView.addObject("mensagem", "Erro ao realizar atualização!");
        }

        return modelAndView;
    }

    @PostMapping("/atualizar-senha")
    public String postAtualizarSenha(FarmaciaDTO farmaciaDTO) {

        // Verifica se existe algum erro nos campos informados pelo usuário.
        if (farmaciaDTO.getSenha().isEmpty() || farmaciaDTO.getConfirmaSenha().isEmpty()) {
            return "redirect:/farmacia/conta?status_senha=campos-erro!";
        }

        // Verifica se as senhas correspondem.
        if (!farmaciaDTO.getSenha().equals(farmaciaDTO.getConfirmaSenha())) {
            return "redirect:/farmacia/conta?status_senha=senhas_nao-correspondem";
        }

        try {

            Farmacia farmacia = getFarmaciaLogada();
            farmacia.setSenha(new BCryptPasswordEncoder().encode(farmaciaDTO.getSenha()));
            farmaciaRepository.save(farmacia);

            return "redirect:/farmacia/conta?status_senha=senha-sucesso";

        } catch (Exception e) {
            System.out.println(e);
            return "redirect:/farmacia/conta?status_senha=senha-erro";
        }

    }

    @GetMapping("/listar-farmacias")
    public ModelAndView getTodosUsuarios() {
        ModelAndView modelAndView = new ModelAndView("farmacia/listar_farmacias");
        var farmacias = farmaciaRepository.findAll();
        modelAndView.addObject("farmacias", farmacias);
        return modelAndView;
    }

    @GetMapping("/buscar")
    public ModelAndView buscarUsuario(@RequestParam String nome) {
        ModelAndView modelAndView = new ModelAndView("farmacia/listar_users");
        var farmacias = farmaciaRepository.findByNomeContaining(nome);
        modelAndView.addObject("farmacias", farmacias);
        return modelAndView;
    }

    // Função para o usuário logado deletar a própria conta
    @GetMapping("/deletar-conta")
    public String deletarConta() {

        try {
            farmaciaRepository.delete(getFarmaciaLogada());
            return "redirect:/logout";
        } catch (Exception e) {
            System.out.println(e);
            return "redirect:/farmacia/conta?status_deletar=erro-deletar-conta";
        }
    }

    //Função apenas para ADMIN deletar uma conta de farmacia
    @GetMapping("/deletar-uma-farmacia/{cnpj}")
    public String deletarUsuario(@PathVariable Long cnpj) {

        var famarcia = farmaciaRepository.findById(cnpj);

        if (famarcia.isPresent()) {
            try {
                farmaciaRepository.delete(famarcia.get());
                return "redirect:/farmacia/listar-farmacias?status=deletado";
            } catch (Exception e) {
                System.out.println(e);
                return "redirect:/farmacia/listar-farmacias?status=erro";
            }
        } else {
            return "redirect:/farmacia/listar-farmacias?status=nao-encontrado";
        }

    }


}
