--
-- PostgreSQL database dump
--

\restrict IBbf1MhkKyXCoaD7o1cDExgfVCOUHe60gSiSblyEymsOI94tFO27V5TXWxFHG0M

-- Dumped from database version 18.3
-- Dumped by pg_dump version 18.3

-- Started on 2026-07-08 12:42:40

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 220 (class 1259 OID 33339)
-- Name: roles; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.roles (
    role_id integer NOT NULL,
    role_name character varying(50) NOT NULL
);


ALTER TABLE public.roles OWNER TO postgres;

--
-- TOC entry 219 (class 1259 OID 33338)
-- Name: roles_role_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.roles_role_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.roles_role_id_seq OWNER TO postgres;

--
-- TOC entry 5031 (class 0 OID 0)
-- Dependencies: 219
-- Name: roles_role_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.roles_role_id_seq OWNED BY public.roles.role_id;


--
-- TOC entry 222 (class 1259 OID 33435)
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    user_id integer NOT NULL,
    name character varying(100) NOT NULL,
    designation character varying(100),
    address text,
    employee_id character varying(50),
    student_code character varying(50),
    class_name character varying(50),
    college character varying(100),
    branch character varying(100),
    section character varying(20),
    email character varying(100) NOT NULL,
    phone_number character varying(15),
    password character varying(255),
    guardian_name character varying(100),
    guardian_phone_number character varying(15),
    role_id integer NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    google_id character varying(100),
    profile_picture text,
    login_type character varying(20)
);


ALTER TABLE public.users OWNER TO postgres;

--
-- TOC entry 221 (class 1259 OID 33434)
-- Name: users_user_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.users_user_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.users_user_id_seq OWNER TO postgres;

--
-- TOC entry 5032 (class 0 OID 0)
-- Dependencies: 221
-- Name: users_user_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.users_user_id_seq OWNED BY public.users.user_id;


--
-- TOC entry 4861 (class 2604 OID 33342)
-- Name: roles role_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.roles ALTER COLUMN role_id SET DEFAULT nextval('public.roles_role_id_seq'::regclass);


--
-- TOC entry 4862 (class 2604 OID 33438)
-- Name: users user_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users ALTER COLUMN user_id SET DEFAULT nextval('public.users_user_id_seq'::regclass);


--
-- TOC entry 5023 (class 0 OID 33339)
-- Dependencies: 220
-- Data for Name: roles; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.roles (role_id, role_name) FROM stdin;
1	SUPER_ADMIN
2	BRANCH_ADMIN
3	STUDENT
\.


--
-- TOC entry 5025 (class 0 OID 33435)
-- Dependencies: 222
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.users (user_id, name, designation, address, employee_id, student_code, class_name, college, branch, section, email, phone_number, password, guardian_name, guardian_phone_number, role_id, created_at, google_id, profile_picture, login_type) FROM stdin;
1	Raghav	Super Admin	Hyderabad	EMP001	\N	\N	\N	\N	\N	raghav@gmail.com	9876543210	admin123	\N	\N	1	2026-07-07 11:36:27.140051	\N	\N	\N
2	Charan	Branch Admin	Hyderabad	EMP002	\N	\N	\N	\N	\N	charan@gmail.com	9876543211	branch123	\N	\N	2	2026-07-07 11:37:02.757748	\N	\N	\N
3	Rahul	\N	\N	\N	ST1001	B.Tech	Spoorthy Engineering College	CSE	A	rahul@gmail.com	9876543212	student123	Ramesh	9876500000	3	2026-07-07 11:37:27.685022	\N	\N	\N
8	Raghavendra	\N	Hyderabad	\N	STU001	4th Year	Spoorthy Engineering College	CSE-DS	A	narsapuramragava@gmail.com	9876543219	\N	Ramesh	9876543211	3	2026-07-08 12:19:41.817217	\N	\N	\N
\.


--
-- TOC entry 5033 (class 0 OID 0)
-- Dependencies: 219
-- Name: roles_role_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.roles_role_id_seq', 3, true);


--
-- TOC entry 5034 (class 0 OID 0)
-- Dependencies: 221
-- Name: users_user_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.users_user_id_seq', 8, true);


--
-- TOC entry 4865 (class 2606 OID 33346)
-- Name: roles roles_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT roles_pkey PRIMARY KEY (role_id);


--
-- TOC entry 4867 (class 2606 OID 33348)
-- Name: roles roles_role_name_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT roles_role_name_key UNIQUE (role_name);


--
-- TOC entry 4869 (class 2606 OID 33450)
-- Name: users users_email_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_email_key UNIQUE (email);


--
-- TOC entry 4871 (class 2606 OID 33452)
-- Name: users users_phone_number_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_phone_number_key UNIQUE (phone_number);


--
-- TOC entry 4873 (class 2606 OID 33448)
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (user_id);


--
-- TOC entry 4874 (class 2606 OID 33453)
-- Name: users fk_role; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT fk_role FOREIGN KEY (role_id) REFERENCES public.roles(role_id);


-- Completed on 2026-07-08 12:42:41

--
-- PostgreSQL database dump complete
--

\unrestrict IBbf1MhkKyXCoaD7o1cDExgfVCOUHe60gSiSblyEymsOI94tFO27V5TXWxFHG0M

