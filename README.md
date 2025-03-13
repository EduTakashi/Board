#  Sistema de Gerenciamento de Quadros

Projeto de boards de tarefas para gerenciar tarefas com java e SQL

## 🗂 Estrutura das Tabelas

###  BOARDS
| Campo | Tipo |
|--------|------|
| id | BIGINT |
| name | VARCHAR(255) NOT NULL |

###  BOARDS_COLUMNS
| Campo | Tipo |
|--------|------|
| id | BIGINT |
| name | VARCHAR(255) NOT NULL |
| order | INT NOT NULL |
| kind | VARCHAR(7) NOT NULL |
| board_id | BIGINT NOT NULL |
|  Foreign Key | `board_id` → `BOARDS(id)` (ON DELETE CASCADE) |
|  Unique Constraint | `board_id` + `order` |

###  CARDS
| Campo | Tipo |
|--------|------|
| id | BIGINT |
| title | VARCHAR(255) NOT NULL |
| description | VARCHAR(255) NOT NULL |
| board_column_id | BIGINT NOT NULL |
| Foreign Key | `board_column_id` → `BOARDS_COLUMNS(id)` (ON DELETE CASCADE) |

###  BLOCKS
| Campo | Tipo |
|--------|------|
| id | BIGINT |
| blocked_at | TIMESTAMP DEFAULT CURRENT_TIMESTAMP |
| block_reason | VARCHAR(255) NOT NULL |
| unblocked_at | TIMESTAMP NULL |
| unblock_reason | VARCHAR(255) NOT NULL |
| card_id | BIGINT NOT NULL |
| Foreign Key | `card_id` → `CARDS(id)` (ON DELETE CASCADE) |

##  Relacionamentos
- Um **BOARD** pode conter várias **BOARDS_COLUMNS** (1:N).
- Uma **BOARDS_COLUMNS** pode conter vários **CARDS** (1:N).
- Um **CARD** pode estar associado a vários **BLOCKS** (1:N).
- A tabela **BLOCKS** está vinculada à tabela **CARDS** através da chave estrangeira `card_id`, com exclusão em cascata.
- A tabela **BOARDS_COLUMNS** inclui uma restrição única para garantir que não existam duas colunas com a mesma ordem dentro do mesmo quadro.


