CREATE DATABASE IF NOT EXISTS Farma_IFSP DEFAULT CHARACTER SET utf8;

use Farma_IFSP;

CREATE TABLE IF NOT EXISTS Permissoes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(50) NOT NULL UNIQUE, -- Ex: gerente, caixa, farmaceutico, balconista
    cadastrar_funcionarios ENUM('sim', 'nao') NOT NULL DEFAULT 'nao',
    comprar_estoque ENUM('sim', 'nao') NOT NULL DEFAULT 'nao',
    vender_produto ENUM('sim', 'nao') NOT NULL DEFAULT 'nao',
    cadastrar_produto ENUM('sim', 'nao') NOT NULL DEFAULT 'nao',
    atualizar_estoque ENUM('sim', 'nao') NOT NULL DEFAULT 'nao',
    visualizar_relatorios ENUM('sim', 'nao') NOT NULL DEFAULT 'nao',
    aprovar_receita ENUM('sim', 'nao') NOT NULL DEFAULT 'nao',
    aplicar_desconto ENUM('sim', 'nao') NOT NULL DEFAULT 'nao'
);

CREATE TABLE if not exists Cargos (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nome VARCHAR(50) UNIQUE NOT NULL,
  id_permissao INT NOT NULL,
  FOREIGN KEY (id_permissao) REFERENCES Permissoes(id)
);


CREATE Table if not exists Funcionarios(
    id INT AUTO_INCREMENT primary key,
    nome VARCHAR(255) NOT NULL,
    cpf VARCHAR(255) NOT NULL UNIQUE,
    matricula VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    telefone VARCHAR(255) NOT NULL,
    senha VARCHAR(255) NOT NULL,
    tipo ENUM('funcionario', 'adm', 'dono') NOT NULL DEFAULT 'funcionario',
    id_cargo int not NULL,    
    atividade VARCHAR(255) NOT NULL DEFAULT 'ativo',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    Foreign Key (id_cargo) REFERENCES Cargos (id)
);

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

create table if not EXISTS Areas_estoque(
    id int AUTO_INCREMENT PRIMARY KEY,
    setor VARCHAR(255) NOT NULL,
    andar VARCHAR(255) NOT NULL,
    tipo_armazenamento ENUM('padrao', 'refrigerado', 'frigorifico')  not NULL DEFAULT 'padrao',
    prateleira VARCHAR(255) NOT NULL
);

create table if not EXISTS Estoque(
    id int AUTO_INCREMENT PRIMARY KEY,
    id_produto int not null,
    id_local int not NULL,
    quantidade int,
    fabricante VARCHAR(255) not null,
    lote VARCHAR(255) NOT NULL,
    data_fabricacao DATE NOT NULL,
    data_validade DATE NOT NULL,
    local_armazenado text,
    nfCompra 	text,
    precoCompra 	decimal(15,2),
    icmsCompra 	decimal(15,2),
    precoVenda 	decimal(15,2),
    desconto ENUM('sim', 'nao') NOT NULL DEFAULT 'nao',
    desconto_padrao decimal(15,2),
    venda_desconto int, -- aqui seria o desconto do convenio que é varival, pode ser valor ou porcentagem
    tipo_venda_desconto ENUM('valor', 'porcentagem') NOT NULL DEFAULT 'valor',
    qtdVendida 	int,
    qtdOcorrencia int,
    qtd_minima INT DEFAULT 0,
    ocorrencia 	varchar(1024),
    Foreign Key (id_produto) REFERENCES Produtos (id), 
    Foreign Key (id_local) REFERENCES Areas_estoque (id)
);

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
    qtdItems int,
    forma_pagamento ENUM('dinheiro', 'cartao_credito', 'cartao_debito', 'pix', 'boleto', 'outro') NOT NULL,
    quantidade_parcelas int,
    dtDevolucao datetime,
    motivoDevolucao text
);

CREATE Table if NOT exists Itens_pedido(
    id int AUTO_INCREMENT PRIMARY KEY,
    id_pedido int not NULL,
    id_estoque int not NULL,
    id_receita int,
    id_produto int not NULL,
    quantidade int not NULL,
    preco_unitario decimal(15,2) NOT NULL,
    sub_total decimal(15,2), -- caso tenha desconto
    Foreign Key (id_produto) REFERENCES Produtos (id), 
    FOREIGN KEY (id_pedido) REFERENCES Pedidos(id),
    Foreign Key (id_estoque) REFERENCES Estoque (id)
);

CREATE TABLE IF NOT EXISTS Receitas(
    id int AUTO_INCREMENT PRIMARY KEY,
    id_funcionario int NOT NULL,
    id_pedido int NOT NULL,
    tipo ENUM('Receita Azul (Receituário de Controle Especial - Tipo A)', 'Receita Verde (Receituário de Controle Especial - Tipo B)', 'Receita Amarela (Notificação de Receita - Tipo A)', 'Receita Branca Comum (Simples)', 'Receita Branca de Controle Especial (Receituário de Controle Especial)') not null, 
    codigo VARCHAR(255) not NULL, 
    cpf_paciente VARCHAR(255) NOT NULL,
    nome_paciente VARCHAR(255) NOT NULL,
    data_nascimento DATE NOT NULL,
    data_validade DATE NOT NULL,
    observacoes TEXT,
    data_emissao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_funcionario) REFERENCES Funcionarios(id),
    FOREIGN KEY (id_pedido) REFERENCES Pedidos(id)
);

CREATE TABLE IF NOT EXISTS Profissional(
    id INT PRIMARY KEY AUTO_INCREMENT,
    id_receita INT NOT NULL,
    nome_profissional VARCHAR(100),
    tipo_registro ENUM('CRM', 'COFEN', 'CRO', 'CRF', 'OUTRO'),
    numero_registro VARCHAR(20),
    uf_registro CHAR(2),
    data_emissao DATE,
    especialidade VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_receita) REFERENCES Receitas(id) ON DELETE CASCADE
);


CREATE TABLE IF NOT EXISTS PedirAcesso(
    id int AUTO_INCREMENT PRIMARY KEY,
    nome_funcionario VARCHAR(255) NOT NULL,
    matricula_funcionario VARCHAR(255) not NULL,
    cargo_funcionario VARCHAR(255) not NULL,
    tipo_acess ENUM('cadastrar_funcionarios', 'comprar_estoque', 'vender_produto', 'cadastrar_produto', 'atualizar_estoque', 'visualizar_relatorios', 'aprovar_receita', 'aplicar_desconto') NOT NULL,
    descricao TEXT NOT NULL,
    status ENUM('pendente', 'aprovado', 'rejeitado') NOT NULL DEFAULT 'pendente',
    data_pedido TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    id_resposta INT DEFAULT NULL, -- ID da resposta associada
    FOREIGN KEY (id_resposta) REFERENCES Respostas(id) ON DELETE SET NULL -- Permite que o ID da resposta seja nulo se a resposta for excluída
);

CREATE TABLE IF NOT EXISTS Respostas(
    id int AUTO_INCREMENT PRIMARY KEY,
    id_pedido int NOT NULL,
    resposta TEXT NOT NULL,
    data_resposta TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    id_cumprimento INT DEFAULT NULL, -- ID do funcionário que irá aprovar ou rejeitar o pedido
    FOREIGN KEY (id_cumprimento) REFERENCES Funcionarios(id) ON DELETE SET NULL -- Permite que o ID do funcionário que aprova/rejeita seja nulo se o funcionário for excluído
    FOREIGN KEY (id_pedido) REFERENCES PedirAcesso(id) ON DELETE CASCADE -- Garante que a resposta seja removida se o pedido de acesso for excluído
);

DROP DATABASE Farma_IFSP;
