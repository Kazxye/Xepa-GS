SELECT
    COALESCE(SUM(rt.peso_kg) FILTER (WHERE o.status = 'RETIRADA'), 0) AS quilos_recuperados,
    COUNT(*) FILTER (WHERE o.status = 'RETIRADA') AS retiradas_concluidas,
    COUNT(DISTINCT r.cozinha_id) FILTER (WHERE o.status = 'RETIRADA') AS cozinhas_atendidas,
    COUNT(DISTINCT o.feirante_id) FILTER (WHERE o.status = 'RETIRADA') AS feirantes_doadores,
    COUNT(*) AS ofertas_publicadas,
    ROUND(
        100.0 * COUNT(*) FILTER (WHERE o.status = 'RETIRADA')
        / NULLIF(COUNT(*) FILTER (WHERE o.status IN ('RETIRADA', 'EXPIRADA')), 0),
        1
    ) AS taxa_de_retirada_percentual,
    ROUND(
        AVG(FLOOR(EXTRACT(EPOCH FROM (r.reservada_em - o.criada_em)) / 60)),
        1
    ) AS tempo_medio_ate_reserva_minutos
FROM oferta o
LEFT JOIN reserva r ON r.oferta_id = o.id
LEFT JOIN retirada rt ON rt.reserva_id = r.id;

SELECT
    f.nome AS feira,
    COALESCE(SUM(rt.peso_kg) FILTER (WHERE o.status = 'RETIRADA'), 0) AS quilos_recuperados,
    COUNT(*) FILTER (WHERE o.status = 'RETIRADA') AS retiradas_concluidas,
    COUNT(*) AS ofertas_publicadas
FROM oferta o
JOIN feirante fe ON fe.id = o.feirante_id
JOIN feira f ON f.id = fe.feira_id
LEFT JOIN reserva r ON r.oferta_id = o.id
LEFT JOIN retirada rt ON rt.reserva_id = r.id
GROUP BY f.id, f.nome
ORDER BY quilos_recuperados DESC, f.nome;

SELECT
    o.tipo AS tipo_alimento,
    COUNT(*) AS retiradas,
    SUM(rt.peso_kg) AS quilos_recuperados,
    ROUND(AVG(rt.peso_kg), 2) AS media_por_retirada_kg
FROM oferta o
JOIN reserva r ON r.oferta_id = o.id
JOIN retirada rt ON rt.reserva_id = r.id
WHERE o.status = 'RETIRADA'
GROUP BY o.tipo
ORDER BY quilos_recuperados DESC;

SELECT
    c.nome AS cozinha,
    COUNT(rt.id) AS retiradas,
    COALESCE(SUM(rt.peso_kg), 0) AS quilos_recebidos,
    MAX(rt.concluida_em) AS ultima_retirada
FROM cozinha c
LEFT JOIN reserva r ON r.cozinha_id = c.id
LEFT JOIN retirada rt ON rt.reserva_id = r.id AND rt.concluida_em IS NOT NULL
GROUP BY c.id, c.nome
ORDER BY quilos_recebidos DESC;

SELECT
    o.id,
    o.descricao,
    o.quantidade_kg,
    o.quantidade_kg - rt.peso_kg AS diferenca_kg,
    ROUND(100.0 * rt.peso_kg / o.quantidade_kg, 1) AS aproveitamento_percentual
FROM oferta o
JOIN reserva r ON r.oferta_id = o.id
JOIN retirada rt ON rt.reserva_id = r.id
WHERE o.status = 'RETIRADA'
ORDER BY aproveitamento_percentual;

SELECT
    c.nome AS cozinha,
    f.nome AS feira,
    ROUND(CAST(6371 * 2 * ASIN(SQRT(
        POWER(SIN(RADIANS(f.latitude - c.latitude) / 2), 2)
        + COS(RADIANS(c.latitude)) * COS(RADIANS(f.latitude))
        * POWER(SIN(RADIANS(f.longitude - c.longitude) / 2), 2)
    )) AS NUMERIC), 2) AS distancia_km,
    c.raio_busca_km
FROM cozinha c
CROSS JOIN feira f
WHERE 6371 * 2 * ASIN(SQRT(
        POWER(SIN(RADIANS(f.latitude - c.latitude) / 2), 2)
        + COS(RADIANS(c.latitude)) * COS(RADIANS(f.latitude))
        * POWER(SIN(RADIANS(f.longitude - c.longitude) / 2), 2)
    )) <= c.raio_busca_km
ORDER BY c.nome, distancia_km;

SELECT
    o.id,
    o.tipo,
    o.descricao,
    o.quantidade_kg,
    o.horario_limite,
    fe.nome_barraca,
    f.nome AS feira
FROM oferta o
JOIN feirante fe ON fe.id = o.feirante_id
JOIN feira f ON f.id = fe.feira_id
WHERE o.status = 'DISPONIVEL'
  AND o.horario_limite > CURRENT_TIMESTAMP
ORDER BY o.horario_limite;
