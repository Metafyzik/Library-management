CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255)
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    roles VARCHAR(50),

    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id)
);
