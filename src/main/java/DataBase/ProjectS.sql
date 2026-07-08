--
-- PostgreSQL database dump
--

\restrict Ca5GPprUR5yXunPAZWgxDRdsKXLrzUJ4Gbt7Cv3GT9DCLwQZ70G1vJ6LJW2iqPk

-- Dumped from database version 18.3
-- Dumped by pg_dump version 18.3

-- Started on 2026-07-08 11:16:49

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
-- TOC entry 224 (class 1259 OID 33376)
-- Name: branch_admin; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.branch_admin (
    branch_admin_id integer NOT NULL,
    name character varying(100) NOT NULL,
    designation character varying(100),
    address text,
    employee_id character varying(50) NOT NULL,
    email character varying(100) NOT NULL,
    phone_number character varying(15) NOT NULL,
    password character varying(255) NOT NULL,
    role_id integer NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.branch_admin OWNER TO postgres;

--
-- TOC entry 223 (class 1259 OID 33375)
-- Name: branch_admin_branch_admin_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.branch_admin_branch_admin_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.branch_admin_branch_admin_id_seq OWNER TO postgres;

--
-- TOC entry 5091 (class 0 OID 0)
-- Dependencies: 223
-- Name: branch_admin_branch_admin_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.branch_admin_branch_admin_id_seq OWNED BY public.branch_admin.branch_admin_id;


--
-- TOC entry 230 (class 1259 OID 33459)
-- Name: question_papers; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.question_papers (
    paper_id integer NOT NULL,
    paper_name character varying(255) NOT NULL,
    image_path text NOT NULL,
    extracted_text text,
    uploaded_by integer NOT NULL,
    uploaded_date timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.question_papers OWNER TO postgres;

--
-- TOC entry 229 (class 1259 OID 33458)
-- Name: question_papers_paper_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.question_papers_paper_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.question_papers_paper_id_seq OWNER TO postgres;

--
-- TOC entry 5092 (class 0 OID 0)
-- Dependencies: 229
-- Name: question_papers_paper_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.question_papers_paper_id_seq OWNED BY public.question_papers.paper_id;


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
-- TOC entry 5093 (class 0 OID 0)
-- Dependencies: 219
-- Name: roles_role_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.roles_role_id_seq OWNED BY public.roles.role_id;


--
-- TOC entry 226 (class 1259 OID 33402)
-- Name: student; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.student (
    student_id integer NOT NULL,
    student_name character varying(100) NOT NULL,
    class character varying(50),
    college character varying(150),
    branch character varying(100),
    section character varying(20),
    student_code character varying(50) NOT NULL,
    email character varying(100) NOT NULL,
    phone_number character varying(15) NOT NULL,
    password character varying(255) NOT NULL,
    guardian_name character varying(100),
    guardian_phone_number character varying(15),
    role_id integer NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.student OWNER TO postgres;

--
-- TOC entry 225 (class 1259 OID 33401)
-- Name: student_student_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.student_student_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.student_student_id_seq OWNER TO postgres;

--
-- TOC entry 5094 (class 0 OID 0)
-- Dependencies: 225
-- Name: student_student_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.student_student_id_seq OWNED BY public.student.student_id;


--
-- TOC entry 222 (class 1259 OID 33350)
-- Name: super_admin; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.super_admin (
    super_admin_id integer NOT NULL,
    name character varying(100) NOT NULL,
    designation character varying(100),
    address text,
    employee_id character varying(50) NOT NULL,
    email character varying(100) NOT NULL,
    phone_number character varying(15) NOT NULL,
    password character varying(255) NOT NULL,
    role_id integer NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.super_admin OWNER TO postgres;

--
-- TOC entry 221 (class 1259 OID 33349)
-- Name: super_admin_super_admin_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.super_admin_super_admin_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.super_admin_super_admin_id_seq OWNER TO postgres;

--
-- TOC entry 5095 (class 0 OID 0)
-- Dependencies: 221
-- Name: super_admin_super_admin_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.super_admin_super_admin_id_seq OWNED BY public.super_admin.super_admin_id;


--
-- TOC entry 228 (class 1259 OID 33435)
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
    password character varying(255) NOT NULL,
    guardian_name character varying(100),
    guardian_phone_number character varying(15),
    role_id integer NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    google_id character varying(100),
    profile_picture text
);


ALTER TABLE public.users OWNER TO postgres;

--
-- TOC entry 227 (class 1259 OID 33434)
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
-- TOC entry 5096 (class 0 OID 0)
-- Dependencies: 227
-- Name: users_user_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.users_user_id_seq OWNED BY public.users.user_id;


--
-- TOC entry 4884 (class 2604 OID 33379)
-- Name: branch_admin branch_admin_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.branch_admin ALTER COLUMN branch_admin_id SET DEFAULT nextval('public.branch_admin_branch_admin_id_seq'::regclass);


--
-- TOC entry 4890 (class 2604 OID 33462)
-- Name: question_papers paper_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.question_papers ALTER COLUMN paper_id SET DEFAULT nextval('public.question_papers_paper_id_seq'::regclass);


--
-- TOC entry 4881 (class 2604 OID 33342)
-- Name: roles role_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.roles ALTER COLUMN role_id SET DEFAULT nextval('public.roles_role_id_seq'::regclass);


--
-- TOC entry 4886 (class 2604 OID 33405)
-- Name: student student_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.student ALTER COLUMN student_id SET DEFAULT nextval('public.student_student_id_seq'::regclass);


--
-- TOC entry 4882 (class 2604 OID 33353)
-- Name: super_admin super_admin_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.super_admin ALTER COLUMN super_admin_id SET DEFAULT nextval('public.super_admin_super_admin_id_seq'::regclass);


--
-- TOC entry 4888 (class 2604 OID 33438)
-- Name: users user_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users ALTER COLUMN user_id SET DEFAULT nextval('public.users_user_id_seq'::regclass);


--
-- TOC entry 5079 (class 0 OID 33376)
-- Dependencies: 224
-- Data for Name: branch_admin; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.branch_admin (branch_admin_id, name, designation, address, employee_id, email, phone_number, password, role_id, created_at) FROM stdin;
1	Shiva	Branch Admin	Hyderabad	EMP101	shiva@gmail.com	9876543221	shiva123	2	2026-07-07 09:47:26.406829
\.


--
-- TOC entry 5085 (class 0 OID 33459)
-- Dependencies: 230
-- Data for Name: question_papers; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.question_papers (paper_id, paper_name, image_path, extracted_text, uploaded_by, uploaded_date) FROM stdin;
\.


--
-- TOC entry 5075 (class 0 OID 33339)
-- Dependencies: 220
-- Data for Name: roles; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.roles (role_id, role_name) FROM stdin;
1	SUPER_ADMIN
2	BRANCH_ADMIN
3	STUDENT
\.


--
-- TOC entry 5081 (class 0 OID 33402)
-- Dependencies: 226
-- Data for Name: student; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.student (student_id, student_name, class, college, branch, section, student_code, email, phone_number, password, guardian_name, guardian_phone_number, role_id, created_at) FROM stdin;
1	Rahul Kumar	B.Tech 4th Year	Spoorthy Engineering College	CSE (Data Science)	A	STU1001	rahul@gmail.com	9876543210	student123	Ramesh Kumar	9876501234	3	2026-07-07 11:05:04.712055
2	Priya Sharma	B.Tech 3rd Year	ABC Engineering College	Information Technology	B	STU1002	priya@gmail.com	9876543211	priya123	Suresh Sharma	9876505678	3	2026-07-07 11:06:21.791278
\.


--
-- TOC entry 5077 (class 0 OID 33350)
-- Dependencies: 222
-- Data for Name: super_admin; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.super_admin (super_admin_id, name, designation, address, employee_id, email, phone_number, password, role_id, created_at) FROM stdin;
1	Charan	Super Admin	Hyderabad	EMP001	charan@gmail.com	9876543211	charan12345	1	2026-07-05 00:33:15.175329
2	Raghav	Super Admin	Hyderabad	EMP002	raghav@gmail.com	9876543210	raghav12345	1	2026-07-05 00:34:00.369279
\.


--
-- TOC entry 5083 (class 0 OID 33435)
-- Dependencies: 228
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.users (user_id, name, designation, address, employee_id, student_code, class_name, college, branch, section, email, phone_number, password, guardian_name, guardian_phone_number, role_id, created_at, google_id, profile_picture) FROM stdin;
1	Raghav	Super Admin	Hyderabad	EMP001	\N	\N	\N	\N	\N	raghav@gmail.com	9876543210	admin123	\N	\N	1	2026-07-07 11:36:27.140051	\N	\N
2	Charan	Branch Admin	Hyderabad	EMP002	\N	\N	\N	\N	\N	charan@gmail.com	9876543211	branch123	\N	\N	2	2026-07-07 11:37:02.757748	\N	\N
3	Rahul	\N	\N	\N	ST1001	B.Tech	Spoorthy Engineering College	CSE	A	rahul@gmail.com	9876543212	student123	Ramesh	9876500000	3	2026-07-07 11:37:27.685022	\N	\N
4	Raghavendra	\N	\N	\N	\N	\N	\N	\N	\N	narsapuramragava@gmail.com	\N	123456	\N	\N	3	2026-07-08 10:41:13.410002	\N	\N
\.


--
-- TOC entry 5097 (class 0 OID 0)
-- Dependencies: 223
-- Name: branch_admin_branch_admin_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.branch_admin_branch_admin_id_seq', 1, true);


--
-- TOC entry 5098 (class 0 OID 0)
-- Dependencies: 229
-- Name: question_papers_paper_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.question_papers_paper_id_seq', 1, false);


--
-- TOC entry 5099 (class 0 OID 0)
-- Dependencies: 219
-- Name: roles_role_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.roles_role_id_seq', 3, true);


--
-- TOC entry 5100 (class 0 OID 0)
-- Dependencies: 225
-- Name: student_student_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.student_student_id_seq', 2, true);


--
-- TOC entry 5101 (class 0 OID 0)
-- Dependencies: 221
-- Name: super_admin_super_admin_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.super_admin_super_admin_id_seq', 2, true);


--
-- TOC entry 5102 (class 0 OID 0)
-- Dependencies: 227
-- Name: users_user_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.users_user_id_seq', 4, true);


--
-- TOC entry 4903 (class 2606 OID 33395)
-- Name: branch_admin branch_admin_email_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.branch_admin
    ADD CONSTRAINT branch_admin_email_key UNIQUE (email);


--
-- TOC entry 4905 (class 2606 OID 33393)
-- Name: branch_admin branch_admin_employee_id_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.branch_admin
    ADD CONSTRAINT branch_admin_employee_id_key UNIQUE (employee_id);


--
-- TOC entry 4907 (class 2606 OID 33391)
-- Name: branch_admin branch_admin_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.branch_admin
    ADD CONSTRAINT branch_admin_pkey PRIMARY KEY (branch_admin_id);


--
-- TOC entry 4921 (class 2606 OID 33471)
-- Name: question_papers question_papers_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.question_papers
    ADD CONSTRAINT question_papers_pkey PRIMARY KEY (paper_id);


--
-- TOC entry 4893 (class 2606 OID 33346)
-- Name: roles roles_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT roles_pkey PRIMARY KEY (role_id);


--
-- TOC entry 4895 (class 2606 OID 33348)
-- Name: roles roles_role_name_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT roles_role_name_key UNIQUE (role_name);


--
-- TOC entry 4909 (class 2606 OID 33421)
-- Name: student student_email_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.student
    ADD CONSTRAINT student_email_key UNIQUE (email);


--
-- TOC entry 4911 (class 2606 OID 33417)
-- Name: student student_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.student
    ADD CONSTRAINT student_pkey PRIMARY KEY (student_id);


--
-- TOC entry 4913 (class 2606 OID 33419)
-- Name: student student_student_code_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.student
    ADD CONSTRAINT student_student_code_key UNIQUE (student_code);


--
-- TOC entry 4897 (class 2606 OID 33369)
-- Name: super_admin super_admin_email_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.super_admin
    ADD CONSTRAINT super_admin_email_key UNIQUE (email);


--
-- TOC entry 4899 (class 2606 OID 33367)
-- Name: super_admin super_admin_employee_id_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.super_admin
    ADD CONSTRAINT super_admin_employee_id_key UNIQUE (employee_id);


--
-- TOC entry 4901 (class 2606 OID 33365)
-- Name: super_admin super_admin_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.super_admin
    ADD CONSTRAINT super_admin_pkey PRIMARY KEY (super_admin_id);


--
-- TOC entry 4915 (class 2606 OID 33450)
-- Name: users users_email_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_email_key UNIQUE (email);


--
-- TOC entry 4917 (class 2606 OID 33452)
-- Name: users users_phone_number_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_phone_number_key UNIQUE (phone_number);


--
-- TOC entry 4919 (class 2606 OID 33448)
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (user_id);


--
-- TOC entry 4923 (class 2606 OID 33396)
-- Name: branch_admin branch_admin_role_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.branch_admin
    ADD CONSTRAINT branch_admin_role_id_fkey FOREIGN KEY (role_id) REFERENCES public.roles(role_id);


--
-- TOC entry 4925 (class 2606 OID 33453)
-- Name: users fk_role; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT fk_role FOREIGN KEY (role_id) REFERENCES public.roles(role_id);


--
-- TOC entry 4926 (class 2606 OID 33472)
-- Name: question_papers fk_uploaded_by; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.question_papers
    ADD CONSTRAINT fk_uploaded_by FOREIGN KEY (uploaded_by) REFERENCES public.users(user_id);


--
-- TOC entry 4924 (class 2606 OID 33422)
-- Name: student student_role_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.student
    ADD CONSTRAINT student_role_id_fkey FOREIGN KEY (role_id) REFERENCES public.roles(role_id);


--
-- TOC entry 4922 (class 2606 OID 33370)
-- Name: super_admin super_admin_role_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.super_admin
    ADD CONSTRAINT super_admin_role_id_fkey FOREIGN KEY (role_id) REFERENCES public.roles(role_id);


-- Completed on 2026-07-08 11:16:50

--
-- PostgreSQL database dump complete
--

\unrestrict Ca5GPprUR5yXunPAZWgxDRdsKXLrzUJ4Gbt7Cv3GT9DCLwQZ70G1vJ6LJW2iqPk

