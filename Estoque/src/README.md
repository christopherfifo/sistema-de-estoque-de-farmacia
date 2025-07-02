# Sistema de Gerenciamento de Estoque de Farmácia

### Christopher Willians Silva Couto - Gu3054047
### Gabriel Vitor Grossi Lourenço - Gu3054446

# Introdução

Criação de um sistema de gerenciamento de estoque para farmácias como projeto final da disciplina de Programação Orientada a Objeto (POO). O intuito deste projeto é o desenvolvimento de um sistema que consiga lidar com o gerenciamento dos funcionários, fluxo de compras e o armazenamento dos produtos, além disso, com uma camada de segurança com base em um sistema de hierarquia de acessos.

Desta forma, como foi requisitado pelo o professor da disciplina, usamos como ferramentas a linguagem de programação o Java para a logica , o banco de dados MySQL para o armazenamento dos dados  e o jQuery para o FrontEnd. Assim, para o cumprimento dos requisitos do projeto, usamos um arquitetura baseada na POO com base em principalmente nas classes do Banco de dados

# Requisitos do Projeto

- Sistema desenvolvido em Java;
- Banco de Dados MySQL;
- Utilização do jQuery para o FrontEnd;
- Código desenvolvido orientado a objeto;
- Criação de um sistema de verificação de acesso (tela de login) que verifique se as credencias informadas coincidem com as cadastradas na database, em casos de incoerências retorne uma mensagem informando que a senha ou matricula estão incorretas;
- Criação de uma tela ou mais para a utilização das funcionalidades com filtro de acesso de acordo com as regras de hierarquia estabelecidas;
- Documentação do projeto com diagrama de uso e fluxograma;

# Banco de Dados

```sql
CREATE DATABASE IF NOT EXISTS Farma_IFSP DEFAULT CHARACTER SET utf8;

use Farma_IFSP;
```

criação do banco de dados no padrão utf8 e o uso do mesmo

## Tabela Permissões

```sql
CREATE TABLE IF NOT EXISTS Permissoes (

    id INT AUTO_INCREMENT PRIMARY KEY,

    nome ENUM('Permissao_Farmaceutico', 'Permissao_Gerente', 'Permissao_Admin', 'Permissao_Caixa') NOT NULL,

    cadastrar_funcionarios BOOLEAN NOT NULL DEFAULT FALSE,
......

);
```

Essa tabela é responsável pela criação de permissões que um determinado funcionário pode ter, desta forma para cada novo funcionário é feita uma inserção nesse tabela com as permissões dele e o tipo de permissão que ele tem, decidimos utilizar essa arquitetura, pois podemos criar funcionários tantos permissões padrões de acordo com um cargo como também personalizar elas.

## Tabelas Cargos

```sql
CREATE TABLE if not exists Cargos (

    id INT AUTO_INCREMENT PRIMARY KEY,

    nome ENUM('Administrador', 'Caixa', 'Farmaceutico', 'Gerente') NOT NULL,

    id_permissao INT NULL,

    FOREIGN KEY (id_permissao) REFERENCES Permissoes(id)

);
```


Essa tabela é onde definimos os cargos que podem existir na farmácia, desta forma, para cada funcionário é feita uma inserção nela e vinculada a linha da ***tabela de Permissões*** (por meio do campo `id_permissao` ) que equivale a um determinado funcionário.

## Tabela de Funcionários

```sql
CREATE Table if not exists Funcionarios(

    id INT AUTO_INCREMENT primary key,

    nome VARCHAR(255) NOT NULL,

    cpf VARCHAR(255) NOT NULL UNIQUE,

    matricula VARCHAR(255) NOT NULL UNIQUE,

    email VARCHAR(255) NOT NULL UNIQUE,

    telefone VARCHAR(255) NOT NULL,

    senha VARCHAR(255) NOT NULL,

    tipo ENUM('funcionario', 'adm', 'dono') NOT NULL DEFAULT 'funcionario',

    id_cargo int NULL,    

    atividade VARCHAR(255) NOT NULL DEFAULT 'ativo',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    Foreign Key (id_cargo) REFERENCES Cargos (id)

);
```

Essa é a tabela responsável por cadastrar as informações do funcionários, nela temos os campos senha e matricula que são usados para validação de acesso ao sistema e funções. Além disso, usamos o campo campo tipo para saber a quem pertence a linha (uma vez que podemos ter administradores que não são o dono do  estabelecimento) e o campo atividade que define se o usuário esta ativo ou não (útil para  saber se ele esta de férias ou foi desligado da empresa).

Nesse contexto, temos a ligação com a ***tabela Cargos*** por meio do campo `id_cargo`, que pro sua é vinculada a um campo da ***tabela de Permissões***, assim criando um encadeamento que tem todas as informações de um colaborador. 

## Tabela de Produtos

```sql
create table if not exists Produtos(

    id INT AUTO_INCREMENT primary key,

    nome VARCHAR(255) NOT NULL,

    descricao VARCHAR(255) NOT NULL,

    armazenamento VARCHAR(255) NOT NULL,

    armazenamento_especial ENUM('sim', 'nao') NOT NULL DEFAULT 'nao',

    receita_obrigatoria ENUM('sim', 'nao') NOT NULL DEFAULT 'nao',

    fabricante VARCHAR(255) NOT NULL,

    categoria ENUM('medicamento', 'antibiotico', 'higiene', 'cosmetico', 'suplemento', 'materiais_medicos', 'infantil', 'dermocosmetico', 'outros') NOT NULL,

    tarja ENUM('vermelha', 'preta', 'amarela', 'isento') NOT NULL DEFAULT 'isento',

    preco DECIMAL(10, 2) NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP

);
```

Essa tabela é onde fica cadastrado todo os produtos da loja, nela temo o 4 campos principais, sendo eles:

 - ***armazenamento_especial:*** onde definimos se o medicamento ou produto necessita de um armazenamento especial, como um local refrigerado por exemplo.
 - ***receita_obrigatoria:*** campo utilizado para produtos que necessitam de receitas para a sua venda, ele é usado para que o vendedor tome ciência que tem que validar e retor a receita.
 - ***categoria:*** usado para saber  a qual grupo de produtos ele pertence.
 - ***tarja:*** para que em caso de receita obrigatória o vendedor tenha o conhecimento do tipo de receita necessária para que a sua venda seja autorizada. 

## Tabela Áreas do Estoque

```sql
create table if not EXISTS Areas_estoque(

    id int AUTO_INCREMENT PRIMARY KEY,

    setor VARCHAR(255) NOT NULL,

    andar VARCHAR(255) NOT NULL,

    tipo_armazenamento ENUM('padrao', 'refrigerado', 'frigorifico')  not NULL DEFAULT 'padrao',

    prateleira VARCHAR(255) NOT NULL

);
```

Essa tabela é responsável por armazenar os locais de armazenamento dos produtos.

## Tabela de Estoque

```sql
create table if not EXISTS Estoque(

    id int AUTO_INCREMENT PRIMARY KEY,

    id_produto int not null,

    id_local int not NULL,

    quantidade int,

    lote VARCHAR(255) NOT NULL,

    data_fabricacao DATE NOT NULL,

    data_validade DATE NOT NULL,

    local_armazenado text,

    nfCompra    text,

    precoCompra     decimal(15,2),

    icmsCompra  decimal(15,2),

    precoVenda  decimal(15,2),

    desconto ENUM('sim', 'nao') NOT NULL DEFAULT 'nao',

    desconto_padrao decimal(15,2),

    venda_desconto int, -- aqui seria o desconto do convenio que é varival, pode ser valor ou porcentagem

    tipo_venda_desconto ENUM('valor', 'porcentagem') NOT NULL DEFAULT 'valor',

    qtdVendida  int,

    qtdOcorrencia int,

    qtd_minima INT DEFAULT 0,

    ocorrencia  varchar(1024),

    Foreign Key (id_produto) REFERENCES Produtos (id),

    Foreign Key (id_local) REFERENCES Areas_estoque (id)

);
```

A tabela `Estoque` armazena todas as informações relacionadas aos **lotes de produtos** disponíveis na farmácia. Cada registro representa um lote específico, controlando desde a **quantidade** até a **validade**, **localização**, **preços**, **descontos**, **vendas** e **ocorrências**.

### Relacionamentos (chaves estrangeiras)

- **`id_produto`** → _Referência à tabela `Produtos`_:  
    Liga o item do estoque ao produto cadastrado. Permite saber o nome, categoria, fabricante, exigência de receita e outras informações importantes do produto associado ao lote.
    
- **`id_local`** →  _Referência à tabela `Areas_estoque`_:  
    Define **onde fisicamente** o produto está armazenado dentro da farmácia (como prateleira, setor, andar e tipo de armazenamento). Esse campo garante controle preciso sobre a localização física do item.


###  Principais Campos

- **`lote`, `data_fabricacao`, `data_validade`:** controle da rastreabilidade e validade.
- **`precoCompra`, `precoVenda`, `icmsCompra`:** controle financeiro e tributário por lote.
- **`desconto`, `desconto_padrao`, `tipo_venda_desconto`:** controle de descontos simples ou por convênios.
- **`qtdVendida`, `qtd_minima`:** controle de saída e alertas de reposição.
- **`ocorrencia`, `qtdOcorrencia`:** registro de problemas como perdas, avarias ou produtos vencidos.
- **`local_armazenado`:** detalhamento do armazenamento caso necessário. 

## Tabela de Pedidos

```c
CREATE TABLE if not exists Pedidos (

    id int AUTO_INCREMENT PRIMARY KEY,

    dtPedido datetime,

    receita ENUM('sim', 'nao') NOT NULL DEFAULT 'nao',

    receita_especial ENUM('sim', 'nao') NOT NULL DEFAULT 'nao',

    dtPagamento datetime,

    dtNotaFiscal datetime,

    notaFiscal text,

    dtRecebimento datetime,

    valorTotal decimal(12,2),

    sub_total DECIMAL(12,2),  

    qtdItems int,

    forma_pagamento ENUM('dinheiro', 'cartao_credito', 'cartao_debito', 'pix', 'boleto', 'outro') NOT NULL,

    quantidade_parcelas int,

    dtDevolucao datetime,

    motivoDevolucao text

);
```

A tabela `Pedidos` é responsável por armazenar os dados de cada venda ou compra realizada na farmácia. Cada linha representa um pedido completo e contém informações como a data do pedido (`dtPedido`), o valor total (`valorTotal`), a quantidade de itens (`qtdItems`) e o subtotal (`sub_total`), que pode refletir descontos aplicados.

Além disso, essa tabela permite controlar detalhes importantes como a forma de pagamento utilizada (`forma_pagamento`) — podendo ser dinheiro, cartão, pix, boleto ou outro — e o número de parcelas (`quantidade_parcelas`) em casos de pagamento a prazo. Também registra o status de devoluções, por meio dos campos `dtDevolucao` e `motivoDevolucao`.

Há dois campos específicos para controle de medicamentos com prescrição: `receita` e `receita_especial`, que indicam se o pedido envolveu algum produto que exige apresentação de receita comum ou controlada. Os campos `dtPagamento`, `dtNotaFiscal` e `dtRecebimento` servem para acompanhar o fluxo completo da transação, desde a emissão até o recebimento e registro fiscal, que pode ser descrito em `notaFiscal`.

Por fim, essa tabela se relaciona com outras como `Itens_pedido`, que detalha os produtos envolvidos em cada pedido, e `Receitas`, no caso de vendas com prescrição médica, permitindo o rastreamento completo de operações comerciais e regulamentadas.
# Classes e Funções

## Gerenciador de Permissões





