# sistema-de-estoque-de-farmacia


# requisitos funcionais

são os as funções que temos que desenvolver, ou seja, a parte do código

- função de leitura de qrcode;


- função de formatação de dados e respostas (input e output);
- função de controle de estoque;
- função de alocação do estoque;
- função de compra de estoque;
- função de alerta de estoque;
- função de busca no estoque;

- função de cadastramento de receitas médicas;
- função de liberação de medicação controlada apenas com a existência da receita médica;
- função de listar o carrinho; 
- função de calculo do total da compra;
- função de aplicação de desconto padrão ou de convenio;
- função de aplicação de desconto personalizado (com  um limite dependendo de quem for aplicar o desconto, se for o adm ou a gerencia não tem limite );
- função da forma de pagamento;
- função que verifica o pagamento;
- função de emissão e criação das notas fiscais; 
- função de cancelamento do pedido;
- função de estorno;
- funções de exibição dos dados;
- funções de respostas;

requisitos ja feitos
- função de login;
- função de verificação de acesso;
- - função de cadastro de gerente ou adm;
- função de cadastro de funcionários e seus níveis de acessos, com a verificação se a pessoa tem autorização para efetuar o cadastro;
- função de atribuição ou revogação de acesso;
  
# requisitos não funcionais

são os agentes externos, como interpretamos e a forma como é inserida a informação pelo usuário, como também a resposta para o usuário, ou seja, tudo que é fator externo.

- **leitor de código de barras:** deve ser homologado pelo o meu programa ou utilizar o sistema padrão de conversão de dados;
- **impressora de notas fiscais:** impressora de uma marca especifica e de modelo especifico, ou uma marca que use o mesmo sistema que os modelos e marcas homologados (se quiser trabalhar com múltiplos modelos, eles terão que ter retro compatibilidade);
- **maquininha de cartão:** tem que aceitar qualquer uma;
