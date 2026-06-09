CREATE TABLE IF NOT EXISTS redirect_addresses (
    id UUID PRIMARY KEY,
    url_to_return_s VARCHAR(1024),
    url_to_return_f VARCHAR(1024),
    url_to_return VARCHAR(1024),
    create_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);
