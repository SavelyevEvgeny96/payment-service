CREATE TYPE bank_enum as ENUM (
    'GPB',
    'AKB_RUS'
);

CREATE CAST (varchar AS bank_enum) WITH INOUT AS IMPLICIT;

CREATE TYPE payment_type_enum as ENUM (
    'SBP',
    'CARD'
);

CREATE CAST (varchar AS payment_type_enum) WITH INOUT AS IMPLICIT;

CREATE TYPE external_system_code_enum as ENUM (
    'LK',
    'ADI',
    'PAY'
);

CREATE CAST (varchar AS external_system_code_enum) WITH INOUT AS IMPLICIT;