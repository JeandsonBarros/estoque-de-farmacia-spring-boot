package com.farmacia.demo.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public class ProdutoDTO {

    private Long id;
    @NotEmpty
    private String nome;
    @NotEmpty
    private String dataDeValidade;
    @NotEmpty
    private String descricao;
    @NotNull
    private MultipartFile imagemFile;
    @NotNull
    private double valor;
    @NotNull
    private int quantidadeDisponivel;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public MultipartFile getImagemFile() {
        return imagemFile;
    }

    public void setImagemFile(MultipartFile imagemFile) {
        this.imagemFile = imagemFile;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDataDeValidade() {
        return dataDeValidade;
    }

    public void setDataDeValidade(String dataDeValidade) {
        this.dataDeValidade = dataDeValidade;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public int getQuantidadeDisponivel() {
        return quantidadeDisponivel;
    }

    public void setQuantidadeDisponivel(int quantidadeDisponivel) {
        this.quantidadeDisponivel = quantidadeDisponivel;
    }
}

