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
    qtdVendida 	int,
    qtdOcorrencia int,
    ocorrencia 	varchar(1024),
    Foreign Key (id_produto) REFERENCES Produtos (id), 
    Foreign Key (id_local) REFERENCES Areas_estoque (id)
);

CREATE TABLE if not exists Pedidos (
    id int AUTO_INCREMENT PRIMARY KEY,
    id_detalhes int not NULL,
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

create Table if not exists Tipo_receitas(
    id int AUTO_INCREMENT PRIMARY KEY,
    id_itensPedidos int NOT NULL,
    tipo ENUM('Receita Azul (Receituário de Controle Especial - Tipo A)', 'Receita Verde (Receituário de Controle Especial - Tipo B)', 'Receita Amarela (Notificação de Receita - Tipo A)', 'Receita Branca Comum (Simples)', 'Receita Branca de Controle Especial (Receituário de Controle Especial)') not null, 
    codigo VARCHAR(255) not NULL, 
    Foreign Key (id_itensPedidos) REFERENCES Itens_pedido (id)
);


DROP DATABASE Farma_IFSP;

-- INSERINDO PERMISSÕES
INSERT INTO Permissoes (nome, cadastrar_funcionarios, comprar_estoque, vender_produto, cadastrar_produto, atualizar_estoque, visualizar_relatorios, aprovar_receita, aplicar_desconto)
VALUES 
('gerente', 'sim', 'sim', 'sim', 'sim', 'sim', 'sim', 'sim', 'sim'),
('farmaceutico', 'nao', 'sim', 'sim', 'sim', 'sim', 'sim', 'sim', 'nao'),
('balconista', 'nao', 'nao', 'sim', 'nao', 'nao', 'nao', 'nao', 'sim'),
('caixa', 'nao', 'nao', 'sim', 'nao', 'nao', 'nao', 'nao', 'nao');

-- INSERINDO CARGOS
INSERT INTO Cargos (nome, id_permissao)
VALUES
('Gerente Geral', 1),
('Farmacêutico Responsável', 2),
('Atendente de Balcão', 3),
('Operador de Caixa', 4);

-- INSERINDO FUNCIONÁRIOS
INSERT INTO Funcionarios (nome, cpf, matricula, email, telefone, senha, tipo, id_cargo)
VALUES 
('João da Silva', '12345678900', 'MAT001', 'joao@empresa.com', '11999998888', 'senha123', 'dono', 1),
('Maria Souza', '98765432100', 'MAT002', 'maria@empresa.com', '11999997777', 'senha123', 'adm', 2),
('Carlos Lima', '11122233344', 'MAT003', 'carlos@empresa.com', '11999996666', 'senha123', 'funcionario', 3),
('Ana Costa', '55566677788', 'MAT004', 'ana@empresa.com', '11999995555', 'senha123', 'funcionario', 4);

-- INSERINDO PRODUTOS
INSERT INTO Produtos (nome, descricao, armazenamento, armazenamento_especial, receita_obrigatoria, fabricante, categoria, tarja, preco)
VALUES 
('Paracetamol 500mg', 'Analgésico e antitérmico', 'local seco', 'nao', 'nao', 'Farmaceutica A', 'medicamento', 'isento', 5.99),
('Amoxicilina 500mg', 'Antibiótico', 'local seco', 'nao', 'sim', 'Farmaceutica B', 'antibiotico', 'vermelha', 12.50);

-- INSERINDO ÁREAS DE ESTOQUE
INSERT INTO Areas_estoque (setor, andar, tipo_armazenamento, prateleira)
VALUES 
('Setor A', 'Térreo', 'padrao', 'Prateleira 1'),
('Setor B', '1º Andar', 'refrigerado', 'Prateleira 2');

-- INSERINDO ESTOQUE
INSERT INTO Estoque (id_produto, id_local, quantidade, fabricante, lote, data_fabricacao, data_validade, local_armazenado, nfCompra, precoCompra, icmsCompra, precoVenda, desconto, desconto_padrao, venda_desconto, qtdVendida, qtdOcorrencia, ocorrencia)
VALUES 
(1, 1, 100, 'Farmaceutica A', 'L123', '2024-01-01', '2026-01-01', 'Setor A - Prateleira 1', 'NF001', 3.00, 0.50, 5.99, 'nao', 0.00, 0, 10, 0, ''),
(2, 2, 50, 'Farmaceutica B', 'L456', '2024-02-01', '2025-02-01', 'Setor B - Prateleira 2', 'NF002', 7.00, 0.80, 12.50, 'sim', 1.00, 10, 5, 0, '');

-- INSERINDO PEDIDOS
-- Exemplo de inserts atualizados para a tabela Pedidos
INSERT INTO Pedidos (
    id_detalhes,
    dtPedido,
    receita,
    receita_especial,
    dtPagamento,
    dtNotaFiscal,
    notaFiscal,
    dtRecebimento,
    valorTotal,
    qtdItems,
    forma_pagamento,
    quantidade_parcelas,
    dtDevolucao,
    motivoDevolucao
) VALUES
(1, '2025-05-14 10:30:00', 'sim', 'nao', '2025-05-14 11:00:00', '2025-05-14 11:05:00', 'NF1234567890', '2025-05-15 09:00:00', 199.90, 3, 'cartao_credito', 2, NULL, NULL),

(2, '2025-05-13 09:15:00', 'nao', 'nao', '2025-05-13 09:30:00', '2025-05-13 09:35:00', 'NF1234567891', '2025-05-14 08:00:00', 59.90, 1, 'dinheiro', 1, NULL, NULL),

(3, '2025-05-10 14:45:00', 'sim', 'sim', '2025-05-10 15:10:00', '2025-05-10 15:15:00', 'NF1234567892', '2025-05-12 13:00:00', 320.50, 5, 'pix', 1, '2025-05-13 10:00:00', 'Produto com defeito');


-- INSERINDO ITENS DO PEDIDO
INSERT INTO Itens_pedido (id_pedido, id_estoque, id_produto, quantidade, preco_unitario, sub_total)
VALUES 
(1, 1, 1, 1, 5.99, 5.99),
(1, 2, 2, 1, 12.50, 12.50);

-- INSERINDO TIPO DE RECEITA
INSERT INTO Tipo_receitas (id_itensPedidos, tipo, codigo)
VALUES 
(2, 'Receita Azul (Receituário de Controle Especial - Tipo A)', 'RX123456');

-- SELECTS SIMPLES
SELECT * FROM Permissoes;
SELECT * FROM Cargos;
SELECT * FROM Funcionarios;
SELECT * FROM Produtos;
SELECT * FROM Areas_estoque;
SELECT * FROM Estoque;
SELECT * FROM Pedidos;
SELECT * FROM Itens_pedido WHERE id = '2';
SELECT * FROM Tipo_receitas;

-- JUNÇÕES (BUSCAS CONJUNTAS)

-- Funcionários com seus cargos e permissões
SELECT f.*, c.nome AS cargo_nome, p.nome AS permissao_nome
FROM Funcionarios f
JOIN Cargos c ON f.id_cargo = c.id
JOIN Permissoes p ON c.id_permissao = p.id;

-- Produtos com informações de estoque e local de armazenamento
SELECT pr.*, e.quantidade, e.lote, e.data_validade, a.setor, a.prateleira
FROM Produtos pr
JOIN Estoque e ON pr.id = e.id_produto
JOIN Areas_estoque a ON e.id_local = a.id;

-- Itens do pedido com detalhes do pedido, produto e estoque
SELECT ip.*, pe.dtPedido, pr.nome AS produto_nome, es.lote, es.data_validade
FROM Itens_pedido ip
JOIN Pedidos pe ON ip.id_pedido = pe.id
JOIN Produtos pr ON ip.id_produto = pr.id
JOIN Estoque es ON ip.id_estoque = es.id;

-- Tipo de receitas com itens do pedido e produto
SELECT tr.*, ip.id_pedido, pr.nome AS produto_nome
FROM Tipo_receitas tr
JOIN Itens_pedido ip ON tr.id_itensPedidos = ip.id
JOIN Produtos pr ON ip.id_produto = pr.id;