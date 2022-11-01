/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author MASTER
 */
public class CarregaUsuarios {

    private Usuario usuarios[];

    /*
        No Método construtor da classe são criados 1000 usuários em memória,
        assim possibilitando simular cadastros. Note que ainda denstro do método
        construtor existe uma função chamada inicializarUsuario, essa função 
        carrega alguns usuários pré-cadastrados já, simulando um banco de dados.
     */
    public CarregaUsuarios() {
        this.usuarios = new Usuario[1000];
        this.incializarUsuario();
    }

    public void incializarUsuario() {

        int x;
        for (x = 0; x < this.usuarios.length; x++) {

            this.usuarios[x] = null;
        }

        Usuario user1 = new Usuario("Alex", "1488880", "123", 1, "Programador",0);
        Usuario user2 = new Usuario("Ive", "4788880", "321", 2, "Designer",0);
        Usuario user3 = new Usuario("Breno", "1202020", "123", 1, "Programador",0);
        Usuario user4 = new Usuario("Teste1", "9999999", "123", 3, "Serviços Gerais",0);
        Usuario user5 = new Usuario("Teste2", "7777777", "123", 3, "Serviços Gerais",0);

        this.usuarios[0] = user1;
        this.usuarios[1] = user2;
        this.usuarios[2] = user3;
        this.usuarios[3] = user4;
        this.usuarios[4] = user5;

    }

    /*
        Localiza o usuário pelo nome e senha
     */
    public Usuario localizarUsuario(String login, String senha) {
        int i;
        for (i = 0; i < this.usuarios.length; i++) {

            if ((this.usuarios[i].getRa().equals(login)) && (this.usuarios[i].getSenha().equals(senha))) {

                return this.usuarios[i];

            }

        }

        return null;
    }

    /*
        Cadastra um novo usuário e o adiciona na primeira posição null que estiver
        disponível no vetor.
     */
    public Usuario gravarUsuario(String login, String ra, String senha, Integer categoria, String descricao) {
        int i;

        for (i = 0; i < this.usuarios.length; i++) {
            if (this.usuarios[i] == null) {

                this.usuarios[i] = new Usuario(login, ra, senha, categoria, descricao,0);

                return this.usuarios[i];
            }

        }

        return null;
    }

    /*
        Verifica se o usuário já foi cadastrado buscando seu RA, caso sim ele 
        retorna true. O break no for é para evitar exceção de ponteiro nulo que
        quebraria a execução no ponto onde a função é chamada.
                
     */

    public boolean localizarUsuarioCadastrado(String login) {
        int i;
        for (i = 0; i < this.usuarios.length; i++) {

            if (this.usuarios[i] == null) {
                break;
            } else if (this.usuarios[i].getRa().equals(login)) {

                return true;

            }

        }

        return false;
    }

}
