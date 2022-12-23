package com.farmacia.demo.controllers;

import com.farmacia.demo.dtos.UsuarioDTO;
import com.farmacia.demo.models.Usuario;
import com.farmacia.demo.repository.UsuarioRepository;
import jakarta.validation.Valid;
import org.hibernate.validator.constraints.br.CPF;
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
@RequestMapping(value = "/usuario/")
public class UsuarioController {

    @Autowired
    UsuarioRepository usuarioRepository;

    private Usuario getUsuarioLogado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return usuarioRepository.findByEmail(auth.getName()).get();
    }

    @GetMapping("/login")
    public String login() {
        return "usuario/login";
    }

    @GetMapping("/cadastro")
    public String getCadastro() {
        return "usuario/cadastro";
    }

    @PostMapping("/cadastro")
    public ModelAndView postCadastro(@Valid UsuarioDTO usuarioDTO, BindingResult result) {

        ModelAndView modelAndView = new ModelAndView("usuario/cadastro");

        // Verifica se existe algum erro nos campos informados pelo usuário.
        if (result.hasErrors()) {
            modelAndView.addObject("mensagem", "Verifique os campos!");
            modelAndView.addObject("usuarioDados", usuarioDTO);
            return modelAndView;
        }

        // Verifica se as senhas correspondem.
        if (!usuarioDTO.getSenha().equals(usuarioDTO.getConfirmaSenha())) {
            modelAndView.addObject("mensagemSenha", "Senhas não correspondem");
            return modelAndView;
        }

        try {

            Usuario usuario = new Usuario();
            BeanUtils.copyProperties(usuarioDTO, usuario);
            usuario.setRole("ADMIN");
            usuario.setSenha(new BCryptPasswordEncoder().encode(usuarioDTO.getSenha()));
            usuarioRepository.save(usuario);

            modelAndView.addObject("mensagem", "Cadastro realizado com sucesso!");

        } catch (Exception e) {
            System.out.println(e);
            modelAndView.addObject("mensagem", "Erro ao realizar cadastro!");
        }

        return modelAndView;
    }

    @GetMapping("/conta")
    public ModelAndView getAtualizarDadosDaConta() {
        ModelAndView modelAndView = new ModelAndView("usuario/dados_da_conta");
        modelAndView.addObject("usuarioDados", getUsuarioLogado());

        return modelAndView;
    }

    @PostMapping("/conta")
    public ModelAndView postAtualizarDadosDaConta(UsuarioDTO usuarioDTO) {

        ModelAndView modelAndView = new ModelAndView("usuario/dados_da_conta");

        // Verifica se existe algum erro nos campos informados pelo usuário.
        if (usuarioDTO.getEmail().isEmpty() || usuarioDTO.getNome().isEmpty() || usuarioDTO.getDataNascimento().isEmpty()) {
            modelAndView.addObject("mensagem", "Verifique os campos!");
            modelAndView.addObject("usuarioDados", usuarioDTO);
            return modelAndView;
        }

        try {

            Usuario usuario = getUsuarioLogado();
            usuario.setEmail(usuarioDTO.getEmail());
            usuario.setNome(usuarioDTO.getNome());
            usuario.setDataNascimento(usuarioDTO.getDataNascimento());
            usuarioRepository.save(usuario);
            modelAndView.addObject("usuarioDados", usuario);
            modelAndView.addObject("mensagem", "Atualização realizada com sucesso!");

        } catch (Exception e) {
            System.out.println(e);
            modelAndView.addObject("usuarioDados", usuarioDTO);
            modelAndView.addObject("mensagem", "Erro ao realizar atualização!");
        }

        return modelAndView;
    }

    @PostMapping("/atualizar-senha")
    public String postAtualizarSenha(UsuarioDTO usuarioDTO) {

        // Verifica se existe algum erro nos campos informados pelo usuário.
        if (usuarioDTO.getSenha().isEmpty() || usuarioDTO.getConfirmaSenha().isEmpty()) {
            return "redirect:/usuario/conta?status_senha=campos-erro!";
        }

        // Verifica se as senhas correspondem.
        if (!usuarioDTO.getSenha().equals(usuarioDTO.getConfirmaSenha())) {
            return "redirect:/usuario/conta?status_senha=senhas_nao-correspondem";
        }

        try {

            Usuario usuario = getUsuarioLogado();
            usuario.setSenha(new BCryptPasswordEncoder().encode(usuarioDTO.getSenha()));
            usuarioRepository.save(usuario);

            return "redirect:/usuario/conta?status_senha=senha-sucesso";

        } catch (Exception e) {
            System.out.println(e);
            return "redirect:/usuario/conta?status_senha=senha-erro";
        }

    }

    @GetMapping("/listar-usuarios")
    public ModelAndView getTodosUsuarios() {
        ModelAndView modelAndView = new ModelAndView("usuario/listar_users");
        var usuarios = usuarioRepository.findAll();
        modelAndView.addObject("usuarios", usuarios);
        return modelAndView;
    }

    @GetMapping("/buscar")
    public ModelAndView buscarUsuario(@RequestParam String nome) {
        ModelAndView modelAndView = new ModelAndView("usuario/listar_users");
        var usuarios = usuarioRepository.findByNomeContaining(nome);
        modelAndView.addObject("usuarios", usuarios);
        return modelAndView;
    }

    // Função para o usuário logado
    @GetMapping("/deletar-conta")
    public String deletarConta() {

            try {
                usuarioRepository.delete(getUsuarioLogado());
                return "redirect:/logout";
            } catch (Exception e) {
                System.out.println(e);
                return "redirect:/usuario/conta?status_deletar=erro-deletar-conta";
            }
    }

    //Função apenas para ADMIN
    @GetMapping("/deletar-um-usuario/{cpf}")
    public String deletarUsuario(@PathVariable Long cpf) {

        var usuario = usuarioRepository.findById(cpf);

        if (usuario.isPresent()) {
            try {
                usuarioRepository.delete(usuario.get());
                return "redirect:/usuario/listar-usuarios?status=deletado";
            } catch (Exception e) {
                System.out.println(e);
                return "redirect:/usuario/listar-usuarios?status=erro";
            }
        } else {
            return "redirect:/usuario/listar-usuarios?status=nao-encontrado";
        }

    }


}
