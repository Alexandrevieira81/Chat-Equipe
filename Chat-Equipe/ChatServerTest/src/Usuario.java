/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author MASTER
 */
public class Usuario {
    private String nome;
    private String ra;
    private String senha;
    private Integer categoria;
    private String descricao;
    private Integer disponibilidade;

    public Usuario(String nome, String ra, String senha, Integer categoria, String descricao,Integer disponibilidade) {
        this.nome = nome;
        this.ra = ra;
        this.senha = senha;
        this.categoria = categoria;
        this.descricao = descricao;
        
    }

    @Override
    public String toString() {
        return "Usuario{" + "nome=" + nome + ", ra=" + ra + ", senha=" + senha + ", categoria=" + categoria + ", descricao=" + descricao + '}';
    }

    public Integer getCategoria() {
        return categoria;
    }

    public String getDescricao() {
        return descricao;
    }

 

    public Usuario() {
        
    }

    public String getRa() {
        return ra;
    }

    public void setRa(String ra) {
        this.ra = ra;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public Integer getDisponibilidade() {
        return disponibilidade;
    }

    public void setDisponibilidade(Integer disponibilidade) {
        this.disponibilidade = disponibilidade;
    }
    
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
    
    
}
