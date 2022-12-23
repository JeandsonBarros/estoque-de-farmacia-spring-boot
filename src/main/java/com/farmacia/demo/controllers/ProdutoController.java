package com.farmacia.demo.controllers;


import com.farmacia.demo.dtos.ProdutoDTO;
import com.farmacia.demo.models.Produto;
import com.farmacia.demo.repository.ProdutoRepository;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
public class ProdutoController {

    @Autowired
    ProdutoRepository produtoRepository;

    //private final String SALVAR_EM_DIR = System.getProperty("user.dir") + "\\uploads\\";
    private final String SALVAR_EM_DIR = System.getProperty("user.dir") + "\\src\\main\\resources\\static\\img\\img_produtos\\";

    @GetMapping("/")
    public ModelAndView getProdutos() {

        var produtos = produtoRepository.findAll();
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
        if(Files.exists(path)) {
          return Files.readAllBytes(path);
        }
        return null;
    }


    @GetMapping("/buscar-produto")
    public ModelAndView buscarProdutoPorNome(@RequestParam String nome){

        var produtos = produtoRepository.findByNomeContaining(nome);
        ModelAndView view = new ModelAndView("index");
        view.addObject("produtos", produtos);

        return view;
    }

    @GetMapping("/cadastrar-produto")
    public String getCasdatrarProduto() {

        return "produto/produto_form";
    }

    @PostMapping("/cadastrar-produto")
    public String postCasdatrarProduto(ProdutoDTO produtoDTO) {

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
            produtoDTO.getImagemFile().transferTo( new File(SALVAR_EM_DIR + nomeArquivo));

            var produto = new Produto();
            BeanUtils.copyProperties(produtoDTO, produto);
            produto.setNomeImagem(nomeArquivo);
            produtoRepository.save(produto);

        } catch (IOException e) {
            System.out.println("ARQUIVO NÃO SALVO");
            e.printStackTrace();
        }

        return "redirect:/";

    }

    @GetMapping("/atualizar-produto/{id}")
    public ModelAndView getAtualizarProduto(@PathVariable Long id) {

        var produto = produtoRepository.findById(id);
        var view = new ModelAndView("produto/produto_form");
        view.addObject("produto", produto.get());

        return view;

    }

    @PostMapping("/atualizar-produto/{id}")
    public String postAtualizarProduto(@PathVariable Long id, ProdutoDTO produtoDTO) {

        try {

            var produto = produtoRepository.findById(id).get();

            // Se for atualizar a imagem
            if (!produtoDTO.getImagemFile().isEmpty()) {

                // Deleta imagem antiga
                Path pathDelete = Paths.get(SALVAR_EM_DIR + produto.getNomeImagem());
                if(Files.exists(pathDelete))
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
                produtoDTO.getImagemFile().transferTo( new File(SALVAR_EM_DIR + nomeArquivo));

                produto.setNomeImagem(nomeArquivo);

            }

            BeanUtils.copyProperties(produtoDTO, produto);
            produto.setId(id);
            produtoRepository.save(produto);

        } catch (IOException e) {
            System.out.println("ARQUIVO NÃO SALVO");
            e.printStackTrace();
        }

        return "redirect:/";

    }

    @GetMapping("/deletar-produto/{id}")
    public String deletarProduto(@PathVariable Long id) {

        try {

            var produto = produtoRepository.findById(id);
            if (!produto.isPresent()) {
                return "redirect:/?status=ERRO";
            }

            Path path = Paths.get(SALVAR_EM_DIR + produto.get().getNomeImagem());
            if(Files.exists(path))
                Files.delete(path);

            produtoRepository.deleteById(id);

            return "redirect:/?status=OK";

        } catch (IOException e) {
            System.out.println("ARQUIVO NÃO APAGADO");
            e.printStackTrace();
            return "redirect:/?status=ERRO";
        }

    }
}
