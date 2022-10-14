/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package principal;

import java.util.Map;
import org.json.simple.JSONObject;

/**
 *
 * @author MASTER
 */


/*
    Essa classe serve para auxiliar o mapeamento da lista de usuários online
    vinda do servidor
*/
public class Usuario extends JSONObject {
    
    private String nome;
    private String ra;
    private String senha;
    private Integer categoria;
    private String descricao;
    private Integer disponibilidade;

    public Usuario(String nome, String ra, String senha, Integer categoria, String descricao) {
        this.nome = nome;
        this.ra = ra;
        this.senha = senha;
        this.categoria = categoria;
        this.descricao = descricao;
        
    }

    public Usuario() {
    }

    public Usuario(Map map) {
        super(map);
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getRa() {
        return ra;
    }

    @Override
    public String toString() {
        return "Usuário: " + nome +" Status: " + (disponibilidade.equals(1)? "Disponível" : "Ocupado") ;
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

    public Integer getCategoria() {
        return categoria;
    }

    public void setCategoria(Integer categoria) {
        this.categoria = categoria;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Integer getDisponibilidade() {
        return disponibilidade;
    }

    public void setDisponibilidade(Integer disponibilidade) {
        this.disponibilidade = disponibilidade;
    }
    
}
