CREATE TABLE loans (
    id BIGSERIAL PRIMARY KEY,
    book_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    loan_date DATE,
    due_date DATE,
    returned BOOLEAN DEFAULT FALSE,

    CONSTRAINT fk_loans_book FOREIGN KEY (book_id) REFERENCES books(id),
    CONSTRAINT fk_loans_user FOREIGN KEY (user_id) REFERENCES users(id)
);