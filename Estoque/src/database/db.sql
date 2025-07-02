DROP DATABASE IF EXISTS Farma_IFSP;
CREATE DATABASE IF NOT EXISTS Farma_IFSP DEFAULT CHARACTER SET utf8;

use Farma_IFSP;

-- Permissões administrativas
-- Permissões de compras e fornecedores
-- Permissões de estoque
-- Permissões de vendas
-- Permissões de descontos e reembolsos
-- Permissões de receitas e controlados
-- Permissões de relatórios
-- Outras permissões
CREATE TABLE IF NOT EXISTS Permissoes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome ENUM('Permissao_Farmaceutico', 'Permissao_Gerente', 'Permissao_Admin', 'Permissao_Caixa') NOT NULL,
    cadastrar_funcionarios BOOLEAN NOT NULL DEFAULT FALSE,
    controlar_acesso_funcionarios BOOLEAN NOT NULL DEFAULT FALSE,
    cadastrar_compras BOOLEAN NOT NULL DEFAULT FALSE,
    cancelar_compras BOOLEAN NOT NULL DEFAULT FALSE,
    gerenciar_fornecedores BOOLEAN NOT NULL DEFAULT FALSE,
    supervisionar_estoque BOOLEAN NOT NULL DEFAULT FALSE,
    atualizar_estoque BOOLEAN NOT NULL DEFAULT FALSE,
    consultar_estoque BOOLEAN NOT NULL DEFAULT FALSE,
    registrar_baixa_estoque BOOLEAN NOT NULL DEFAULT FALSE,
    registrar_venda_receita BOOLEAN NOT NULL DEFAULT FALSE,
    registrar_venda_simples BOOLEAN NOT NULL DEFAULT FALSE,
    finalizar_venda BOOLEAN NOT NULL DEFAULT FALSE,
    emitir_nota_fiscal BOOLEAN NOT NULL DEFAULT FALSE,
    registrar_pagamento BOOLEAN NOT NULL DEFAULT FALSE,
    aplicar_desconto BOOLEAN NOT NULL DEFAULT FALSE,
    aplicar_desconto_simples BOOLEAN NOT NULL DEFAULT FALSE,
    autorizar_reembolso BOOLEAN NOT NULL DEFAULT FALSE,
    solicitar_reembolso BOOLEAN NOT NULL DEFAULT FALSE,
    analisar_receita BOOLEAN NOT NULL DEFAULT FALSE,
    autorizar_controlados BOOLEAN NOT NULL DEFAULT FALSE,
    relatorio_financeiro BOOLEAN NOT NULL DEFAULT FALSE,
    relatorio_vendas_diarias BOOLEAN NOT NULL DEFAULT FALSE,
    gerar_orcamento BOOLEAN NOT NULL DEFAULT FALSE,
    indicar_medicamento BOOLEAN NOT NULL DEFAULT FALSE,
    solicitar_autorizacao BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE if not exists Cargos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome ENUM('Administrador', 'Caixa', 'Farmaceutico', 'Gerente') NOT NULL,
    id_permissao INT NULL,
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
    id_cargo int NULL,    
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

ALTER TABLE Pedidos ADD COLUMN sub_total DECIMAL(12,2) AFTER valorTotal;

-- inserts

INSERT INTO Permissoes (
    nome,
    cadastrar_funcionarios,
    controlar_acesso_funcionarios,  
    cadastrar_compras,
    cancelar_compras,
    gerenciar_fornecedores,
    supervisionar_estoque,
    atualizar_estoque,
    consultar_estoque,
    registrar_baixa_estoque,
    registrar_venda_receita,
    registrar_venda_simples,
    finalizar_venda,
    emitir_nota_fiscal,
    registrar_pagamento,
    aplicar_desconto,
    aplicar_desconto_simples,
    autorizar_reembolso,
    solicitar_reembolso,
    analisar_receita,
    autorizar_controlados,
    relatorio_financeiro,
    relatorio_vendas_diarias,
    gerar_orcamento,
    indicar_medicamento,
    solicitar_autorizacao
) VALUES
('Permissao_Farmaceutico', 0, 0, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1),
('Permissao_Gerente', 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1),
('Permissao_Admin', 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
('Permissao_Caixa', 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 1, 1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0);

INSERT INTO Cargos ( nome, id_permissao) VALUES
( 'Administrador', 1),
( 'Gerente', 2),
( 'Farmaceutico', 3),
( 'Caixa', 4);

INSERT INTO Funcionarios (nome, cpf, matricula, email, telefone, senha, tipo, id_cargo, atividade) VALUES
('Gabriel Admin', '111.111.111-11', 'admin', 'admin@farma.com', '11911111111', 'admin123', 'adm', 1, 'ativo'),
('Christopher Gerente', '222.222.222-22', 'gerente', 'gerente@farma.com', '11922222222', 'gerente123', 'funcionario', 2, 'ativo'),
('Joao Farmaceutico', '333.333.333-33', 'farma', 'farma@farma.com', '11933333333', 'farma123', 'funcionario', 3, 'ativo'),
('Jessica Caixa', '444.444.444-44', 'caixa', 'caixa@farma.com', '11944444444', 'caixa123', 'funcionario', 4, 'ativo');

SELECT * from Funcionarios; 

INSERT INTO Produtos (nome, descricao, armazenamento, armazenamento_especial, receita_obrigatoria, fabricante, categoria, tarja, preco) VALUES
('Dipirona Sódica 500mg', 'Analgésico e antitérmico. Caixa com 10 comprimidos.', 'Temperatura ambiente', 'nao', 'nao', 'Medley', 'medicamento', 'vermelha', 12.50),
('Amoxicilina 500mg', 'Antibiótico. Caixa com 21 cápsulas.', 'Temperatura ambiente', 'nao', 'sim', 'EMS', 'antibiotico', 'vermelha', 35.75),
('Protetor Solar FPS 50', 'Protetor solar para pele sensível.', 'Temperatura ambiente', 'nao', 'nao', 'Nivea', 'dermocosmetico', 'isento', 45.90),
('Shampoo Anticaspa', 'Controle de caspa e oleosidade. 200ml.', 'Temperatura ambiente', 'nao', 'nao', 'Head & Shoulders', 'higiene', 'isento', 22.00),
('Insulina Humana', 'Tratamento de diabetes.', 'Refrigerado 2-8°C', 'sim', 'sim', 'Eli Lilly', 'medicamento', 'vermelha', 78.30);

INSERT INTO Areas_estoque (setor, andar, tipo_armazenamento, prateleira) VALUES
('Corredor A', 'Térreo', 'padrao', 'A1'),
('Corredor B', 'Térreo', 'padrao', 'B3'),
('Controlados', 'Balcão', 'padrao', 'C1-Trancado'),
('Refrigerados', 'Depósito', 'refrigerado', 'Geladeira Farma-01');

INSERT INTO Estoque (id_produto, id_local, quantidade, fabricante, lote, data_fabricacao, data_validade, precoVenda) VALUES
(1, 1, 150, 'Medley', 'LOTE_DIP202401', '2024-01-15', '2026-01-15', 12.50),
(2, 3, 80, 'EMS', 'LOTE_AMX202403', '2024-03-20', '2025-03-20', 35.75),
(3, 2, 120, 'Nivea', 'LOTE_PROT202311', '2023-11-01', '2025-11-01', 45.90),
(4, 2, 200, 'Head & Shoulders', 'LOTE_SHA202405', '2024-05-10', '2027-05-10', 22.00),
(5, 4, 40, 'Eli Lilly', 'LOTE_INS202406', '2024-06-01', '2025-06-01', 78.30);