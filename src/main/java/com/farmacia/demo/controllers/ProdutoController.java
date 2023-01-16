package com.farmacia.demo.controllers;

import com.farmacia.demo.dtos.ProdutoDTO;
import com.farmacia.demo.models.Farmacia;
import com.farmacia.demo.models.Produto;
import com.farmacia.demo.repository.FarmaciaRepository;
import com.farmacia.demo.repository.ProdutoRepository;

import jakarta.validation.Valid;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;

@Controller
public class ProdutoController {

    @Autowired
    ProdutoRepository produtoRepository;

    @Autowired
    FarmaciaRepository farmaciaRepository;

    //private final String SALVAR_EM_DIR = System.getProperty("user.dir") + "\\uploads\\";
    private final String SALVAR_EM_DIR = System.getProperty("user.dir") + "\\src\\main\\resources\\static\\img\\img_produtos\\";

    private Farmacia getFarmaciaLogada() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return farmaciaRepository.findByEmail(auth.getName()).get();
    }

    @GetMapping("/")
    public ModelAndView getProdutos() {

        var produtos = produtoRepository.findByFarmacia(getFarmaciaLogada());
        ModelAndView view = new ModelAndView("index");
        view.addObject("produtos", produtos);

        return view;
    }

    @GetMapping(
            value = "/get-file/{imagemNome}",
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    public @ResponseBody byte[] getFile(@PathVariable String imagemNome) throws IOException {

        Path path = Paths.get(SALVAR_EM_DIR + imagemNome);
        if (Files.exists(path)) {
            return Files.readAllBytes(path);
        }
        return null;
    }

    @GetMapping("/buscar-produto")
    public ModelAndView buscarProdutoPorNome(@RequestParam String nome) {

        var produtos = produtoRepository.findByFarmaciaAndNomeContaining(getFarmaciaLogada(), nome);
        ModelAndView view = new ModelAndView("index");
        view.addObject("produtos", produtos);

        return view;
    }

    @GetMapping("/cadastrar-produto")
    public String getCasdatrarProduto() {
        return "produto/produto_form";
    }

    @PostMapping("/cadastrar-produto")
    public ModelAndView postCasdatrarProduto(@Valid ProdutoDTO produtoDTO, BindingResult result) {

        ModelAndView modelAndView = new ModelAndView("produto/produto_form");

        // Verifica se existe algum erro nos campos informados pelo usuário.
        if (result.hasErrors()) {
            modelAndView.addObject("mensagem", "Verifique os campos!");
            modelAndView.addObject("produto", produtoDTO);
            return modelAndView;
        }

        try {

            String nomeArquivo = produtoDTO.getImagemFile().getOriginalFilename();

            // Verifica se arquivo já existe
            int mais = 1;
            while (true) {
                File f = new File(SALVAR_EM_DIR + nomeArquivo);
                if (f.exists() && !f.isDirectory()) {
                    mais++;
                    nomeArquivo = Integer.toString(mais) + nomeArquivo;
                } else {
                    break;
                }
            }

            // Salva imagem
            produtoDTO.getImagemFile().transferTo(new File(SALVAR_EM_DIR + nomeArquivo));

            var produto = new Produto();
            BeanUtils.copyProperties(produtoDTO, produto);
            produto.setFarmacia(getFarmaciaLogada());
            produto.setDataDeValidade(new SimpleDateFormat("yyyy-MM-dd").parse(produtoDTO.getDataDeValidade()));
            produto.setNomeImagem(nomeArquivo);
            produtoRepository.save(produto);
            modelAndView.addObject("mensagem", "Produto salvo!");

        } catch (IOException e) {
            System.out.println("ARQUIVO NÃO SALVO");
            modelAndView.addObject("mensagem", "Erro ao salvar produto!");
        } catch (ParseException e) {
            modelAndView.addObject("mensagem", "Erro na data de validade fornecida!");
            throw new RuntimeException(e);
        }

        return modelAndView;

    }

    @GetMapping("/atualizar-produto/{id}")
    public ModelAndView getAtualizarProduto(@PathVariable Long id) {

        ModelAndView modelAndView;
        var produto = produtoRepository.findByFarmaciaAndId(getFarmaciaLogada(), id);

        if (produto.isPresent()) {
            modelAndView = new ModelAndView("produto/produto_form");
            modelAndView.addObject("produto", produto.get());
        } else {
            modelAndView = new ModelAndView("erro404");
        }

        return modelAndView;

    }

    @PostMapping("/atualizar-produto/{id}")
    public ModelAndView postAtualizarProduto(@PathVariable Long id, @Valid ProdutoDTO produtoDTO, BindingResult result) {

        ModelAndView modelAndView;
        var produto = produtoRepository.findByFarmaciaAndId(getFarmaciaLogada(), id);

        if (produto.isPresent()) {
            modelAndView = new ModelAndView("produto/produto_form");
        } else {
            modelAndView = new ModelAndView("erro404");
            return modelAndView;
        }

        // Verifica se existe algum erro nos campos informados pelo usuário.
        if (result.hasErrors()) {
            modelAndView.addObject("mensagem", "Verifique os campos!");
            BeanUtils.copyProperties(produtoDTO, produto.get());
            modelAndView.addObject("produto", produto.get());
            return modelAndView;
        }

        try {

            // Se for atualizar a imagem
            if (!produtoDTO.getImagemFile().isEmpty()) {

                // Deleta imagem antiga
                Path pathDelete = Paths.get(SALVAR_EM_DIR + produto.get().getNomeImagem());
                if (Files.exists(pathDelete))
                    Files.delete(pathDelete);

                // Verifica se arquivo já existe e cria um novo nome caso já exista
                String nomeArquivo = produtoDTO.getImagemFile().getOriginalFilename();
                int mais = 1;
                while (true) {
                    File f = new File(SALVAR_EM_DIR + nomeArquivo);
                    if (f.exists() && !f.isDirectory()) {
                        mais++;
                        nomeArquivo = Integer.toString(mais) + nomeArquivo;
                    } else {
                        break;
                    }
                }

                // Salva a nova imagem
                produtoDTO.getImagemFile().transferTo(new File(SALVAR_EM_DIR + nomeArquivo));
                produto.get().setNomeImagem(nomeArquivo);

            }

            BeanUtils.copyProperties(produtoDTO, produto.get());
            produto.get().setId(id);
            produto.get().setDataDeValidade(new SimpleDateFormat("yyyy-MM-dd").parse(produtoDTO.getDataDeValidade()));
            produtoRepository.save(produto.get());
            modelAndView.addObject("mensagem", "Produto atualizado!");


        } catch (IOException e) {
            System.out.println("ARQUIVO NÃO SALVO");
            modelAndView.addObject("mensagem", "Erro ao atualizar produto!");
        }catch (ParseException e) {
            modelAndView.addObject("mensagem", "Erro na data de validade fornecida!");
            throw new RuntimeException(e);
        }

        modelAndView.addObject("produto", produto.get());
        return modelAndView;

    }

    @GetMapping("/deletar-produto/{id}")
    public String deletarProduto(@PathVariable Long id) {

        try {

            var produto = produtoRepository.findByFarmaciaAndId(getFarmaciaLogada(), id);
            if (!produto.isPresent()) {
                return "erro404";
            }

            Path path = Paths.get(SALVAR_EM_DIR + produto.get().getNomeImagem());
            if (Files.exists(path))
                Files.delete(path);

            produtoRepository.delete(produto.get());

            return "redirect:/?status=OK";

        } catch (IOException e) {
            System.out.println("ARQUIVO NÃO APAGADO");
            e.printStackTrace();
            return "redirect:/?status=ERRO";
        }

    }
}
