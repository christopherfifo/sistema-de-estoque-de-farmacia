import auxiliares.Funcionario;

public class App {
    public static void main(String[] args) throws Exception {
        Funcionario usuarioLogado = Funcionario.login("123456", "senha123");

        if (usuarioLogado != null) {
            System.out.println("Login bem-sucedido!");
            System.out.println("Bem-vindo, " + usuarioLogado.getNome() + "!");
            System.out.println("Matrícula: " + usuarioLogado.getMatricula());
            System.out.println("Cargo: " + usuarioLogado.getNomeCargo());
        } else {
            System.out.println("Falha no login. Verifique matrícula e senha.");
        }
    }
}
