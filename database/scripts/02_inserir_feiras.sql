INSERT INTO feira (id, nome, endereco, latitude, longitude, dia_semana) VALUES
    (1, 'Feira da Rua Joaquim Távora', 'Rua Joaquim Távora, Vila Mariana, São Paulo', -23.5876, -46.6349, 'TUESDAY'),
    (2, 'Feira da Rua Doutor Mário Cardim', 'Rua Doutor Mário Cardim, Vila Mariana, São Paulo', -23.5846, -46.6388, 'WEDNESDAY'),
    (3, 'Feira da Rua Capitão Macedo', 'Rua Capitão Macedo, Vila Mariana, São Paulo', -23.5905, -46.6326, 'THURSDAY'),
    (4, 'Feira da Rua Humberto I', 'Rua Humberto I, Vila Mariana, São Paulo', -23.5921, -46.6402, 'FRIDAY'),
    (5, 'Feira da Rua Áurea', 'Rua Áurea, Vila Mariana, São Paulo', -23.5952, -46.6371, 'SATURDAY');

ALTER TABLE feira ALTER COLUMN id RESTART WITH 100;
