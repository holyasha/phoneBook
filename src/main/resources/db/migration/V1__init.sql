CREATE TABLE department (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    short_name VARCHAR(255) NOT NULL,
    type VARCHAR(255) NOT NULL,
    CONSTRAINT chk_department_type CHECK (
        type IN ('DEPARTMENT', 'ADMINISTRATION', 'OFFICE')
    )
);

CREATE TABLE employee (
    id BIGSERIAL PRIMARY KEY,
    last_name VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    middle_name VARCHAR(255),
    department_id BIGINT NOT NULL,
    office_number VARCHAR(255),
    work_phone VARCHAR(255) NOT NULL,
    personal_phone VARCHAR(255),
    email_address VARCHAR(255) NOT NULL,
    status_note VARCHAR(255),
    additional_info TEXT,
    is_active BOOLEAN NOT NULL,

    CONSTRAINT fk_employee_department
        FOREIGN KEY (department_id)
        REFERENCES department(id)
);

ALTER TABLE employee
    ADD CONSTRAINT uk_employee_work_phone UNIQUE (work_phone);

ALTER TABLE employee
    ADD CONSTRAINT uk_employee_email UNIQUE (email_address);

CREATE TABLE user_account (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL,
    department_id BIGINT,

    CONSTRAINT chk_user_role CHECK (
        role IN ('USER', 'MODERATOR', 'ADMIN')
    ),

    CONSTRAINT fk_user_department
        FOREIGN KEY (department_id)
        REFERENCES department(id)
);

ALTER TABLE user_account
    ADD CONSTRAINT uk_user_username UNIQUE (username);