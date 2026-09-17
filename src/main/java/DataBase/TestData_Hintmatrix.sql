--
-- PostgreSQL database dump
--

\restrict Uij8OcWTHaRxCnRk0zIX57cNqhsYADUvqLihD4sVbEzDuTeGlcpGCmYZuaFb38I

-- Dumped from database version 18.4
-- Dumped by pg_dump version 18.4

-- Started on 2026-09-10 10:30:50

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

--
-- TOC entry 2 (class 3079 OID 30020)
-- Name: pgcrypto; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS pgcrypto WITH SCHEMA public;


--
-- TOC entry 5480 (class 0 OID 0)
-- Dependencies: 2
-- Name: EXTENSION pgcrypto; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION pgcrypto IS 'cryptographic functions';


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 220 (class 1259 OID 30058)
-- Name: answer_events; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.answer_events (
    answer_event_id bigint NOT NULL,
    description text,
    is_correct boolean DEFAULT false,
    user_answer text,
    hint text,
    active_row boolean DEFAULT true,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    user_id bigint,
    question_id bigint,
    attribute_id bigint,
    arithmetic character varying(50),
    event_type character varying(30),
    attempt_number integer,
    marks numeric(10,2),
    answer_position integer,
    option_id bigint
);


ALTER TABLE public.answer_events OWNER TO postgres;

--
-- TOC entry 221 (class 1259 OID 30068)
-- Name: answer_events_answer_event_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.answer_events_answer_event_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.answer_events_answer_event_id_seq OWNER TO postgres;

--
-- TOC entry 5481 (class 0 OID 0)
-- Dependencies: 221
-- Name: answer_events_answer_event_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.answer_events_answer_event_id_seq OWNED BY public.answer_events.answer_event_id;


--
-- TOC entry 222 (class 1259 OID 30069)
-- Name: attendance; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.attendance (
    attendance_id bigint NOT NULL,
    student_id character varying(255) NOT NULL,
    roll_no character varying(255) NOT NULL,
    student_name character varying(255) NOT NULL,
    section character varying(255),
    attendance_date date NOT NULL,
    day character varying(255),
    in_time time without time zone,
    out_time time without time zone,
    status character varying(255),
    status_description character varying(255),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.attendance OWNER TO postgres;

--
-- TOC entry 223 (class 1259 OID 30081)
-- Name: attendance_attendance_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.attendance_attendance_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.attendance_attendance_id_seq OWNER TO postgres;

--
-- TOC entry 5482 (class 0 OID 0)
-- Dependencies: 223
-- Name: attendance_attendance_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.attendance_attendance_id_seq OWNED BY public.attendance.attendance_id;


--
-- TOC entry 224 (class 1259 OID 30082)
-- Name: branch; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.branch (
    branch_id bigint NOT NULL,
    college_id bigint NOT NULL,
    branch_name character varying(255) NOT NULL,
    address character varying(255),
    phone_number character varying(255),
    email character varying(255),
    active_row boolean DEFAULT true NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.branch OWNER TO postgres;

--
-- TOC entry 225 (class 1259 OID 30096)
-- Name: branch_branch_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.branch_branch_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.branch_branch_id_seq OWNER TO postgres;

--
-- TOC entry 5483 (class 0 OID 0)
-- Dependencies: 225
-- Name: branch_branch_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.branch_branch_id_seq OWNED BY public.branch.branch_id;


--
-- TOC entry 226 (class 1259 OID 30097)
-- Name: chapters; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.chapters (
    chapter_id bigint NOT NULL,
    course_id bigint NOT NULL,
    name character varying(255) NOT NULL,
    active_row boolean DEFAULT true,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    row_status integer DEFAULT 1,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    subject_id bigint NOT NULL
);


ALTER TABLE public.chapters OWNER TO postgres;

--
-- TOC entry 227 (class 1259 OID 30108)
-- Name: chapters_chapter_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.chapters_chapter_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.chapters_chapter_id_seq OWNER TO postgres;

--
-- TOC entry 5484 (class 0 OID 0)
-- Dependencies: 227
-- Name: chapters_chapter_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.chapters_chapter_id_seq OWNED BY public.chapters.chapter_id;


--
-- TOC entry 228 (class 1259 OID 30109)
-- Name: college; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.college (
    college_id bigint NOT NULL,
    institute_name character varying(255) NOT NULL,
    address character varying(255),
    phone_number character varying(255),
    email character varying(255),
    active_row boolean DEFAULT true NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.college OWNER TO postgres;

--
-- TOC entry 229 (class 1259 OID 30122)
-- Name: college_college_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.college_college_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.college_college_id_seq OWNER TO postgres;

--
-- TOC entry 5485 (class 0 OID 0)
-- Dependencies: 229
-- Name: college_college_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.college_college_id_seq OWNED BY public.college.college_id;


--
-- TOC entry 230 (class 1259 OID 30123)
-- Name: courses; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.courses (
    course_id bigint NOT NULL,
    name character varying(255) NOT NULL,
    active_row boolean DEFAULT true,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    row_status integer DEFAULT 1,
    branch_id bigint,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    college_id bigint
);


ALTER TABLE public.courses OWNER TO postgres;

--
-- TOC entry 231 (class 1259 OID 30133)
-- Name: courses_course_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.courses_course_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.courses_course_id_seq OWNER TO postgres;

--
-- TOC entry 5486 (class 0 OID 0)
-- Dependencies: 231
-- Name: courses_course_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.courses_course_id_seq OWNED BY public.courses.course_id;


--
-- TOC entry 232 (class 1259 OID 30134)
-- Name: question_answers; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.question_answers (
    answer_id bigint NOT NULL,
    user_id bigint NOT NULL,
    question_id bigint NOT NULL,
    table_name_id bigint,
    header_id bigint,
    attribute_id bigint,
    arithmetic character varying(255),
    amount numeric(38,2),
    active_row boolean DEFAULT true,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    row_status integer DEFAULT 1,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    condition_id bigint,
    total_answers bigint,
    pair_attribute_id bigint,
    option_id bigint,
    answer_text text
);


ALTER TABLE public.question_answers OWNER TO postgres;

--
-- TOC entry 233 (class 1259 OID 30146)
-- Name: exam_answers_answer_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.exam_answers_answer_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.exam_answers_answer_id_seq OWNER TO postgres;

--
-- TOC entry 5487 (class 0 OID 0)
-- Dependencies: 233
-- Name: exam_answers_answer_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.exam_answers_answer_id_seq OWNED BY public.question_answers.answer_id;


--
-- TOC entry 234 (class 1259 OID 30147)
-- Name: exam_chapters; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.exam_chapters (
    exam_id bigint NOT NULL,
    chapter_id bigint NOT NULL
);


ALTER TABLE public.exam_chapters OWNER TO postgres;

--
-- TOC entry 235 (class 1259 OID 30152)
-- Name: exam_questions; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.exam_questions (
    exam_question_id bigint NOT NULL,
    exam_id bigint NOT NULL,
    question_id bigint NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.exam_questions OWNER TO postgres;

--
-- TOC entry 236 (class 1259 OID 30159)
-- Name: exam_questions_exam_question_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.exam_questions_exam_question_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.exam_questions_exam_question_id_seq OWNER TO postgres;

--
-- TOC entry 5488 (class 0 OID 0)
-- Dependencies: 236
-- Name: exam_questions_exam_question_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.exam_questions_exam_question_id_seq OWNED BY public.exam_questions.exam_question_id;


--
-- TOC entry 237 (class 1259 OID 30160)
-- Name: question_attributes; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.question_attributes (
    question_attribute_id bigint NOT NULL,
    question_id bigint NOT NULL,
    header_id bigint NOT NULL,
    attribute_id bigint NOT NULL,
    transaction_date date,
    amount numeric(18,2),
    amount2 numeric(18,2),
    note text,
    active_row boolean DEFAULT true,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.question_attributes OWNER TO postgres;

--
-- TOC entry 238 (class 1259 OID 30172)
-- Name: exam_questions_question_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.exam_questions_question_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.exam_questions_question_id_seq OWNER TO postgres;

--
-- TOC entry 5489 (class 0 OID 0)
-- Dependencies: 238
-- Name: exam_questions_question_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.exam_questions_question_id_seq OWNED BY public.question_attributes.question_attribute_id;


--
-- TOC entry 239 (class 1259 OID 30173)
-- Name: exam_result; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.exam_result (
    exam_result_id bigint NOT NULL,
    exam_id bigint NOT NULL,
    user_id bigint NOT NULL,
    total_marks numeric(10,2) NOT NULL,
    percentage numeric(5,2) NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.exam_result OWNER TO postgres;

--
-- TOC entry 240 (class 1259 OID 30182)
-- Name: exam_result_exam_result_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.exam_result_exam_result_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.exam_result_exam_result_id_seq OWNER TO postgres;

--
-- TOC entry 5490 (class 0 OID 0)
-- Dependencies: 240
-- Name: exam_result_exam_result_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.exam_result_exam_result_id_seq OWNED BY public.exam_result.exam_result_id;


--
-- TOC entry 241 (class 1259 OID 30183)
-- Name: exams; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.exams (
    exam_id bigint NOT NULL,
    exam_name character varying(255) NOT NULL,
    college_id bigint NOT NULL,
    branch_id bigint NOT NULL,
    course_id bigint NOT NULL,
    section_id bigint NOT NULL,
    start_date timestamp without time zone NOT NULL,
    end_date timestamp without time zone NOT NULL,
    active_row boolean DEFAULT true,
    row_status integer DEFAULT 1,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    pass_percentage integer NOT NULL
);


ALTER TABLE public.exams OWNER TO postgres;

--
-- TOC entry 242 (class 1259 OID 30199)
-- Name: questions; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.questions (
    question_id bigint NOT NULL,
    chapter_id bigint NOT NULL,
    topic_id bigint CONSTRAINT questions_category_id_not_null NOT NULL,
    question_text character varying(255) NOT NULL,
    active_row boolean DEFAULT true,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    course_id bigint,
    question_type_id bigint,
    subject_id bigint NOT NULL
);


ALTER TABLE public.questions OWNER TO postgres;

--
-- TOC entry 243 (class 1259 OID 30210)
-- Name: exams_exam_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.exams_exam_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.exams_exam_id_seq OWNER TO postgres;

--
-- TOC entry 5491 (class 0 OID 0)
-- Dependencies: 243
-- Name: exams_exam_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.exams_exam_id_seq OWNED BY public.questions.question_id;


--
-- TOC entry 244 (class 1259 OID 30211)
-- Name: exams_exam_id_seq1; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.exams_exam_id_seq1
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.exams_exam_id_seq1 OWNER TO postgres;

--
-- TOC entry 5492 (class 0 OID 0)
-- Dependencies: 244
-- Name: exams_exam_id_seq1; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.exams_exam_id_seq1 OWNED BY public.exams.exam_id;


--
-- TOC entry 245 (class 1259 OID 30212)
-- Name: mcq_options; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.mcq_options (
    option_id bigint NOT NULL,
    question_id bigint NOT NULL,
    option_order integer NOT NULL,
    option_text character varying(255) NOT NULL,
    is_correct boolean DEFAULT false NOT NULL,
    active_row boolean DEFAULT true NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.mcq_options OWNER TO postgres;

--
-- TOC entry 246 (class 1259 OID 30225)
-- Name: mcq_options_option_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.mcq_options_option_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.mcq_options_option_id_seq OWNER TO postgres;

--
-- TOC entry 5493 (class 0 OID 0)
-- Dependencies: 246
-- Name: mcq_options_option_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.mcq_options_option_id_seq OWNED BY public.mcq_options.option_id;


--
-- TOC entry 247 (class 1259 OID 30226)
-- Name: mcq_questions; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.mcq_questions (
    question_id bigint NOT NULL,
    marks double precision DEFAULT 1.00 NOT NULL,
    active_row boolean DEFAULT true NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    question_type_id bigint NOT NULL
);


ALTER TABLE public.mcq_questions OWNER TO postgres;

--
-- TOC entry 248 (class 1259 OID 30237)
-- Name: plan_courses; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.plan_courses (
    plan_course_id bigint NOT NULL,
    plan_id bigint NOT NULL,
    course_id bigint NOT NULL
);


ALTER TABLE public.plan_courses OWNER TO postgres;

--
-- TOC entry 249 (class 1259 OID 30243)
-- Name: plan_courses_plan_course_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.plan_courses_plan_course_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.plan_courses_plan_course_id_seq OWNER TO postgres;

--
-- TOC entry 5494 (class 0 OID 0)
-- Dependencies: 249
-- Name: plan_courses_plan_course_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.plan_courses_plan_course_id_seq OWNED BY public.plan_courses.plan_course_id;


--
-- TOC entry 250 (class 1259 OID 30244)
-- Name: topic; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.topic (
    topic_id bigint CONSTRAINT question_categories_category_id_not_null NOT NULL,
    topic_name character varying(150) CONSTRAINT question_categories_name_not_null NOT NULL,
    active_row boolean DEFAULT true,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    row_status integer DEFAULT 1,
    order_of integer DEFAULT 1,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    course_id bigint CONSTRAINT question_categories_course_id_not_null NOT NULL,
    chapter_id bigint CONSTRAINT question_categories_chapter_id_not_null NOT NULL,
    subject_id bigint CONSTRAINT question_categories_subject_id_not_null NOT NULL
);


ALTER TABLE public.topic OWNER TO postgres;

--
-- TOC entry 251 (class 1259 OID 30257)
-- Name: question_categories_category_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.question_categories_category_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.question_categories_category_id_seq OWNER TO postgres;

--
-- TOC entry 5495 (class 0 OID 0)
-- Dependencies: 251
-- Name: question_categories_category_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.question_categories_category_id_seq OWNED BY public.topic.topic_id;


--
-- TOC entry 252 (class 1259 OID 30258)
-- Name: question_type; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.question_type (
    question_type_id bigint NOT NULL,
    question_type character varying(255) NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.question_type OWNER TO postgres;

--
-- TOC entry 253 (class 1259 OID 30264)
-- Name: question_type_question_type_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.question_type_question_type_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.question_type_question_type_id_seq OWNER TO postgres;

--
-- TOC entry 5496 (class 0 OID 0)
-- Dependencies: 253
-- Name: question_type_question_type_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.question_type_question_type_id_seq OWNED BY public.question_type.question_type_id;


--
-- TOC entry 254 (class 1259 OID 30265)
-- Name: questions_question_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.questions_question_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.questions_question_id_seq OWNER TO postgres;

--
-- TOC entry 5497 (class 0 OID 0)
-- Dependencies: 254
-- Name: questions_question_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.questions_question_id_seq OWNED BY public.questions.question_id;


--
-- TOC entry 255 (class 1259 OID 30266)
-- Name: roles; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.roles (
    role_id integer NOT NULL,
    role_name character varying(50) NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.roles OWNER TO postgres;

--
-- TOC entry 256 (class 1259 OID 30273)
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
-- TOC entry 5498 (class 0 OID 0)
-- Dependencies: 256
-- Name: roles_role_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.roles_role_id_seq OWNED BY public.roles.role_id;


--
-- TOC entry 257 (class 1259 OID 30274)
-- Name: rule_engines; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.rule_engines (
    rule_engine_id bigint NOT NULL,
    chapter_id bigint NOT NULL,
    pair_attribute_id bigint,
    relationship_name character varying(100),
    pair_order integer,
    arithmetic1 character varying(20),
    table1_id bigint,
    header1_id bigint,
    amount_position1 character varying(20),
    information1 text,
    arithmetic2 character varying(20),
    table2_id bigint,
    header2_id bigint,
    amount_position2 character varying(20),
    information2 text,
    arithmetic3 character varying(20),
    table3_id bigint,
    header3_id bigint,
    amount_position3 character varying(20),
    information3 text,
    arithmetic4 character varying(20),
    table4_id bigint,
    header4_id bigint,
    amount_position4 character varying(20),
    information4 text,
    active_row boolean DEFAULT true,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    row_status integer DEFAULT 1,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    attribute_id bigint
);


ALTER TABLE public.rule_engines OWNER TO postgres;

--
-- TOC entry 258 (class 1259 OID 30285)
-- Name: rule_engines_rule_engine_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.rule_engines_rule_engine_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.rule_engines_rule_engine_id_seq OWNER TO postgres;

--
-- TOC entry 5499 (class 0 OID 0)
-- Dependencies: 258
-- Name: rule_engines_rule_engine_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.rule_engines_rule_engine_id_seq OWNED BY public.rule_engines.rule_engine_id;


--
-- TOC entry 259 (class 1259 OID 30286)
-- Name: section; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.section (
    section_id bigint NOT NULL,
    course_id bigint NOT NULL,
    section_name character varying(255) NOT NULL,
    description character varying(255),
    active_row boolean DEFAULT true NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    college_id bigint,
    branch_id bigint
);


ALTER TABLE public.section OWNER TO postgres;

--
-- TOC entry 260 (class 1259 OID 30300)
-- Name: section_section_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.section_section_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.section_section_id_seq OWNER TO postgres;

--
-- TOC entry 5500 (class 0 OID 0)
-- Dependencies: 260
-- Name: section_section_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.section_section_id_seq OWNED BY public.section.section_id;


--
-- TOC entry 261 (class 1259 OID 30301)
-- Name: subject; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.subject (
    subject_id bigint NOT NULL,
    subject_name character varying(255) NOT NULL,
    course_id bigint NOT NULL,
    active_row boolean DEFAULT true,
    row_status integer DEFAULT 1,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.subject OWNER TO postgres;

--
-- TOC entry 262 (class 1259 OID 30311)
-- Name: subject_subject_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.subject_subject_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.subject_subject_id_seq OWNER TO postgres;

--
-- TOC entry 5501 (class 0 OID 0)
-- Dependencies: 262
-- Name: subject_subject_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.subject_subject_id_seq OWNED BY public.subject.subject_id;


--
-- TOC entry 263 (class 1259 OID 30312)
-- Name: subscription_plans; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.subscription_plans (
    plan_id bigint NOT NULL,
    name character varying(100) NOT NULL,
    description character varying(255),
    free_trial boolean DEFAULT false NOT NULL,
    active boolean DEFAULT true NOT NULL,
    duration_days integer NOT NULL,
    practice_question_limit integer NOT NULL,
    mock_test_enabled boolean DEFAULT false NOT NULL,
    mock_test_limit integer,
    exam_enabled boolean DEFAULT false NOT NULL,
    exam_attempt_limit integer,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.subscription_plans OWNER TO postgres;

--
-- TOC entry 264 (class 1259 OID 30331)
-- Name: subscription_plans_plan_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.subscription_plans_plan_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.subscription_plans_plan_id_seq OWNER TO postgres;

--
-- TOC entry 5502 (class 0 OID 0)
-- Dependencies: 264
-- Name: subscription_plans_plan_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.subscription_plans_plan_id_seq OWNED BY public.subscription_plans.plan_id;


--
-- TOC entry 265 (class 1259 OID 30332)
-- Name: table_attributes; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.table_attributes (
    attribute_id bigint NOT NULL,
    header_id bigint NOT NULL,
    name character varying(255) NOT NULL,
    row_status character varying(255) DEFAULT 'DRAFT'::character varying,
    active_row boolean DEFAULT true,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    amount1 bigint,
    amount2 bigint,
    row_disable boolean DEFAULT false
);


ALTER TABLE public.table_attributes OWNER TO postgres;

--
-- TOC entry 266 (class 1259 OID 30345)
-- Name: table_attributes_attribute_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.table_attributes_attribute_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.table_attributes_attribute_id_seq OWNER TO postgres;

--
-- TOC entry 5503 (class 0 OID 0)
-- Dependencies: 266
-- Name: table_attributes_attribute_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.table_attributes_attribute_id_seq OWNED BY public.table_attributes.attribute_id;


--
-- TOC entry 267 (class 1259 OID 30346)
-- Name: table_headers; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.table_headers (
    header_id bigint NOT NULL,
    name character varying(255) NOT NULL,
    active_row boolean DEFAULT true,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    row_status integer DEFAULT 1,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.table_headers OWNER TO postgres;

--
-- TOC entry 268 (class 1259 OID 30355)
-- Name: table_headers_header_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.table_headers_header_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.table_headers_header_id_seq OWNER TO postgres;

--
-- TOC entry 5504 (class 0 OID 0)
-- Dependencies: 268
-- Name: table_headers_header_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.table_headers_header_id_seq OWNED BY public.table_headers.header_id;


--
-- TOC entry 269 (class 1259 OID 30356)
-- Name: table_names; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.table_names (
    table_name_id bigint NOT NULL,
    name character varying(255) NOT NULL,
    active_row boolean DEFAULT true,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    row_status integer DEFAULT 1,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.table_names OWNER TO postgres;

--
-- TOC entry 270 (class 1259 OID 30365)
-- Name: table_names_table_name_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.table_names_table_name_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.table_names_table_name_id_seq OWNER TO postgres;

--
-- TOC entry 5505 (class 0 OID 0)
-- Dependencies: 270
-- Name: table_names_table_name_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.table_names_table_name_id_seq OWNED BY public.table_names.table_name_id;


--
-- TOC entry 271 (class 1259 OID 30366)
-- Name: user_subscriptions; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_subscriptions (
    subscription_id bigint NOT NULL,
    user_id bigint NOT NULL,
    plan_id bigint NOT NULL,
    course_id bigint NOT NULL,
    starts_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    expires_at timestamp without time zone,
    active boolean DEFAULT true NOT NULL,
    practice_questions_used integer DEFAULT 0 NOT NULL,
    mock_tests_used integer DEFAULT 0 NOT NULL,
    exam_attempts_used integer DEFAULT 0 NOT NULL
);


ALTER TABLE public.user_subscriptions OWNER TO postgres;

--
-- TOC entry 272 (class 1259 OID 30383)
-- Name: user_subscriptions_subscription_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.user_subscriptions_subscription_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.user_subscriptions_subscription_id_seq OWNER TO postgres;

--
-- TOC entry 5506 (class 0 OID 0)
-- Dependencies: 272
-- Name: user_subscriptions_subscription_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.user_subscriptions_subscription_id_seq OWNED BY public.user_subscriptions.subscription_id;


--
-- TOC entry 273 (class 1259 OID 30928)
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    user_id bigint NOT NULL,
    name character varying(100) NOT NULL,
    designation character varying(100),
    address text,
    employee_id bigint,
    student_id bigint,
    course_id bigint,
    college_id bigint,
    branch_id bigint,
    section_id bigint,
    email character varying(255) NOT NULL,
    phone_number character varying(15),
    password character varying(255),
    guardian_name character varying(100),
    guardian_phone_number character varying(15),
    role_id integer NOT NULL,
    google_id character varying(255),
    profile_picture text,
    login_type character varying(20) DEFAULT 'NORMAL'::character varying,
    active_row boolean DEFAULT true,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.users OWNER TO postgres;

--
-- TOC entry 274 (class 1259 OID 30941)
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
-- TOC entry 5507 (class 0 OID 0)
-- Dependencies: 274
-- Name: users_user_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.users_user_id_seq OWNED BY public.users.user_id;


--
-- TOC entry 5028 (class 2604 OID 30398)
-- Name: answer_events answer_event_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.answer_events ALTER COLUMN answer_event_id SET DEFAULT nextval('public.answer_events_answer_event_id_seq'::regclass);


--
-- TOC entry 5033 (class 2604 OID 30399)
-- Name: attendance attendance_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.attendance ALTER COLUMN attendance_id SET DEFAULT nextval('public.attendance_attendance_id_seq'::regclass);


--
-- TOC entry 5036 (class 2604 OID 30400)
-- Name: branch branch_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.branch ALTER COLUMN branch_id SET DEFAULT nextval('public.branch_branch_id_seq'::regclass);


--
-- TOC entry 5040 (class 2604 OID 30401)
-- Name: chapters chapter_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.chapters ALTER COLUMN chapter_id SET DEFAULT nextval('public.chapters_chapter_id_seq'::regclass);


--
-- TOC entry 5045 (class 2604 OID 30402)
-- Name: college college_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.college ALTER COLUMN college_id SET DEFAULT nextval('public.college_college_id_seq'::regclass);


--
-- TOC entry 5049 (class 2604 OID 30403)
-- Name: courses course_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.courses ALTER COLUMN course_id SET DEFAULT nextval('public.courses_course_id_seq'::regclass);


--
-- TOC entry 5059 (class 2604 OID 30404)
-- Name: exam_questions exam_question_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.exam_questions ALTER COLUMN exam_question_id SET DEFAULT nextval('public.exam_questions_exam_question_id_seq'::regclass);


--
-- TOC entry 5065 (class 2604 OID 30405)
-- Name: exam_result exam_result_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.exam_result ALTER COLUMN exam_result_id SET DEFAULT nextval('public.exam_result_exam_result_id_seq'::regclass);


--
-- TOC entry 5067 (class 2604 OID 30406)
-- Name: exams exam_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.exams ALTER COLUMN exam_id SET DEFAULT nextval('public.exams_exam_id_seq1'::regclass);


--
-- TOC entry 5076 (class 2604 OID 30407)
-- Name: mcq_options option_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.mcq_options ALTER COLUMN option_id SET DEFAULT nextval('public.mcq_options_option_id_seq'::regclass);


--
-- TOC entry 5085 (class 2604 OID 30408)
-- Name: plan_courses plan_course_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.plan_courses ALTER COLUMN plan_course_id SET DEFAULT nextval('public.plan_courses_plan_course_id_seq'::regclass);


--
-- TOC entry 5054 (class 2604 OID 30409)
-- Name: question_answers answer_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.question_answers ALTER COLUMN answer_id SET DEFAULT nextval('public.exam_answers_answer_id_seq'::regclass);


--
-- TOC entry 5061 (class 2604 OID 30410)
-- Name: question_attributes question_attribute_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.question_attributes ALTER COLUMN question_attribute_id SET DEFAULT nextval('public.exam_questions_question_id_seq'::regclass);


--
-- TOC entry 5092 (class 2604 OID 30411)
-- Name: question_type question_type_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.question_type ALTER COLUMN question_type_id SET DEFAULT nextval('public.question_type_question_type_id_seq'::regclass);


--
-- TOC entry 5072 (class 2604 OID 30412)
-- Name: questions question_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.questions ALTER COLUMN question_id SET DEFAULT nextval('public.questions_question_id_seq'::regclass);


--
-- TOC entry 5094 (class 2604 OID 30413)
-- Name: roles role_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.roles ALTER COLUMN role_id SET DEFAULT nextval('public.roles_role_id_seq'::regclass);


--
-- TOC entry 5097 (class 2604 OID 30414)
-- Name: rule_engines rule_engine_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.rule_engines ALTER COLUMN rule_engine_id SET DEFAULT nextval('public.rule_engines_rule_engine_id_seq'::regclass);


--
-- TOC entry 5102 (class 2604 OID 30415)
-- Name: section section_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.section ALTER COLUMN section_id SET DEFAULT nextval('public.section_section_id_seq'::regclass);


--
-- TOC entry 5106 (class 2604 OID 30416)
-- Name: subject subject_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.subject ALTER COLUMN subject_id SET DEFAULT nextval('public.subject_subject_id_seq'::regclass);


--
-- TOC entry 5111 (class 2604 OID 30417)
-- Name: subscription_plans plan_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.subscription_plans ALTER COLUMN plan_id SET DEFAULT nextval('public.subscription_plans_plan_id_seq'::regclass);


--
-- TOC entry 5118 (class 2604 OID 30418)
-- Name: table_attributes attribute_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.table_attributes ALTER COLUMN attribute_id SET DEFAULT nextval('public.table_attributes_attribute_id_seq'::regclass);


--
-- TOC entry 5124 (class 2604 OID 30419)
-- Name: table_headers header_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.table_headers ALTER COLUMN header_id SET DEFAULT nextval('public.table_headers_header_id_seq'::regclass);


--
-- TOC entry 5129 (class 2604 OID 30420)
-- Name: table_names table_name_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.table_names ALTER COLUMN table_name_id SET DEFAULT nextval('public.table_names_table_name_id_seq'::regclass);


--
-- TOC entry 5086 (class 2604 OID 30421)
-- Name: topic topic_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.topic ALTER COLUMN topic_id SET DEFAULT nextval('public.question_categories_category_id_seq'::regclass);


--
-- TOC entry 5134 (class 2604 OID 30422)
-- Name: user_subscriptions subscription_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_subscriptions ALTER COLUMN subscription_id SET DEFAULT nextval('public.user_subscriptions_subscription_id_seq'::regclass);


--
-- TOC entry 5140 (class 2604 OID 30942)
-- Name: users user_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users ALTER COLUMN user_id SET DEFAULT nextval('public.users_user_id_seq'::regclass);


--
-- TOC entry 5420 (class 0 OID 30058)
-- Dependencies: 220
-- Data for Name: answer_events; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.answer_events (answer_event_id, description, is_correct, user_answer, hint, active_row, created_at, updated_at, user_id, question_id, attribute_id, arithmetic, event_type, attempt_number, marks, answer_position, option_id) FROM stdin;
1	from debit particulars of Opening Stock is 2000 >> attempted to ADD on liabilities side of Balance Sheet.	f	attempted to ADD on liabilities side of Balance Sheet.	\N	f	2026-09-08 18:24:16.823289	2026-09-08 18:24:16.823289	1	3	1	ADD	ANSWER	1	0.00	1	\N
2	MCQ attempt	t	Trading Account	\N	t	2026-09-08 21:05:30.601876	2026-09-08 21:05:30.601876	2	8	\N	\N	MCQ_ANSWER	1	1.00	\N	1
\.


--
-- TOC entry 5422 (class 0 OID 30069)
-- Dependencies: 222
-- Data for Name: attendance; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.attendance (attendance_id, student_id, roll_no, student_name, section, attendance_date, day, in_time, out_time, status, status_description, created_at, updated_at) FROM stdin;
\.


--
-- TOC entry 5424 (class 0 OID 30082)
-- Dependencies: 224
-- Data for Name: branch; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.branch (branch_id, college_id, branch_name, address, phone_number, email, active_row, created_at, updated_at) FROM stdin;
1	1	KPHB	KPHB Colony, Hyderabad, Telangana - 500072	9876543211	kphb@narayanajunior.edu.in	t	2026-09-08 16:52:00.144405	2026-09-08 16:52:00.144405
2	1	Madhapur	Madhapur, Hyderabad, Telangana - 500081	9876543212	madhapur@narayanajunior.edu.in	t	2026-09-08 16:52:40.641873	2026-09-08 16:52:40.641873
3	1	Narayanaguda	Narayanaguda, Hyderabad, Telangana - 500029	9876543213	narayanaguda@narayanajunior.edu.in	t	2026-09-08 16:53:44.627595	2026-09-08 16:53:44.627595
\.


--
-- TOC entry 5426 (class 0 OID 30097)
-- Dependencies: 226
-- Data for Name: chapters; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.chapters (chapter_id, course_id, name, active_row, created_at, row_status, updated_at, subject_id) FROM stdin;
1	1	Final Accounts without Adjustments	t	2026-09-08 16:59:12.582145	1	2026-09-08 16:59:12.582145	3
2	1	Journal Basic Practice	t	2026-09-08 16:59:47.523608	1	2026-09-08 16:59:47.523608	3
3	1	Rectification of Errors for Jr.Inter, Class11, CA-Found & B.com	t	2026-09-08 17:05:38.101743	1	2026-09-08 17:05:38.10674	3
4	1	Final Accounts with Adjustments	t	2026-09-08 17:09:51.455934	1	2026-09-08 17:09:51.455934	3
5	1	Journal Entries for Jr.Inter, Class 11, CA-Foundation and B.com	t	2026-09-08 17:10:27.545085	1	2026-09-08 17:10:27.545085	3
6	1	Rectification of Errors Basic Practice	t	2026-09-08 17:10:45.353606	1	2026-09-08 17:10:45.353606	3
\.


--
-- TOC entry 5428 (class 0 OID 30109)
-- Dependencies: 228
-- Data for Name: college; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.college (college_id, institute_name, address, phone_number, email, active_row, created_at, updated_at) FROM stdin;
1	Narayana Junior College	Kukatpally, Hyderabad, Telangana - 500072	9876543210	kukatpally@narayanajunior.edu.in	t	2026-09-08 16:48:58.819695	2026-09-08 16:48:58.822128
\.


--
-- TOC entry 5430 (class 0 OID 30123)
-- Dependencies: 230
-- Data for Name: courses; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.courses (course_id, name, active_row, created_at, row_status, branch_id, updated_at, college_id) FROM stdin;
1	Jr.Inter	t	2026-09-08 16:56:05.570285	1	1	2026-09-08 16:56:05.570285	1
\.


--
-- TOC entry 5434 (class 0 OID 30147)
-- Dependencies: 234
-- Data for Name: exam_chapters; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.exam_chapters (exam_id, chapter_id) FROM stdin;
\.


--
-- TOC entry 5435 (class 0 OID 30152)
-- Dependencies: 235
-- Data for Name: exam_questions; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.exam_questions (exam_question_id, exam_id, question_id, created_at) FROM stdin;
\.


--
-- TOC entry 5439 (class 0 OID 30173)
-- Dependencies: 239
-- Data for Name: exam_result; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.exam_result (exam_result_id, exam_id, user_id, total_marks, percentage, created_at) FROM stdin;
\.


--
-- TOC entry 5441 (class 0 OID 30183)
-- Dependencies: 241
-- Data for Name: exams; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.exams (exam_id, exam_name, college_id, branch_id, course_id, section_id, start_date, end_date, active_row, row_status, created_at, updated_at, pass_percentage) FROM stdin;
\.


--
-- TOC entry 5445 (class 0 OID 30212)
-- Dependencies: 245
-- Data for Name: mcq_options; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.mcq_options (option_id, question_id, option_order, option_text, is_correct, active_row, created_at, updated_at) FROM stdin;
1	8	1	Trading Account	t	t	2026-09-08 20:39:51.07186	2026-09-08 20:39:51.07186
2	8	2	Profit and Loss Account	f	t	2026-09-08 20:39:51.07186	2026-09-08 20:39:51.07186
3	8	3	Balance Sheet	f	t	2026-09-08 20:39:51.07186	2026-09-08 20:39:51.07186
4	8	4	Capital Account	f	t	2026-09-08 20:39:51.07186	2026-09-08 20:39:51.07186
5	9	1	Credit Side	f	t	2026-09-09 10:09:05.818697	2026-09-09 10:09:05.818697
6	9	2	Debit Side	t	t	2026-09-09 10:09:05.818697	2026-09-09 10:09:05.818697
7	9	3	Both Sides	f	t	2026-09-09 10:09:05.818697	2026-09-09 10:09:05.818697
8	9	4	Neither Side	f	t	2026-09-09 10:09:05.818697	2026-09-09 10:09:05.818697
9	10	1	Opening Stock	t	t	2026-09-09 10:09:06.062918	2026-09-09 10:09:06.062918
10	10	2	Purchases	t	t	2026-09-09 10:09:06.062918	2026-09-09 10:09:06.062918
11	10	3	Direct Wages	t	t	2026-09-09 10:09:06.062918	2026-09-09 10:09:06.062918
12	10	4	Sales	f	t	2026-09-09 10:09:06.062918	2026-09-09 10:09:06.062918
13	11	1	Net Sales − Cost of Goods Sold	t	t	2026-09-09 10:09:06.106078	2026-09-09 10:09:06.106078
14	11	2	Purchases − Sales	f	t	2026-09-09 10:09:06.106078	2026-09-09 10:09:06.106078
15	11	3	Sales + Purchases	f	t	2026-09-09 10:09:06.106078	2026-09-09 10:09:06.106078
16	11	4	Net Profit − Expenses	f	t	2026-09-09 10:09:06.106078	2026-09-09 10:09:06.106078
17	12	1	Carriage Inward	t	t	2026-09-09 10:09:06.14873	2026-09-09 10:09:06.14873
18	12	2	Direct Wages	t	t	2026-09-09 10:09:06.14873	2026-09-09 10:09:06.14873
19	12	3	Factory Power	t	t	2026-09-09 10:09:06.14873	2026-09-09 10:09:06.14873
20	12	4	Office Salary	f	t	2026-09-09 10:09:06.14873	2026-09-09 10:09:06.14873
21	13	1	Cash Account	f	t	2026-09-09 10:09:06.189382	2026-09-09 10:09:06.189382
22	13	2	Profit and Loss Account	t	t	2026-09-09 10:09:06.189382	2026-09-09 10:09:06.189382
23	13	3	Purchases Account	f	t	2026-09-09 10:09:06.189382	2026-09-09 10:09:06.189382
24	13	4	Capital Account	f	t	2026-09-09 10:09:06.189382	2026-09-09 10:09:06.189382
25	14	1	Sales	t	t	2026-09-09 10:09:06.221794	2026-09-09 10:09:06.221794
26	14	2	Closing Stock	t	t	2026-09-09 10:09:06.221794	2026-09-09 10:09:06.221794
27	14	3	Purchases	f	t	2026-09-09 10:09:06.221794	2026-09-09 10:09:06.221794
28	14	4	Opening Stock	f	t	2026-09-09 10:09:06.221794	2026-09-09 10:09:06.221794
29	15	1	Trading Account	f	t	2026-09-09 10:09:06.25114	2026-09-09 10:09:06.25114
30	15	2	Balance Sheet	f	t	2026-09-09 10:09:06.25114	2026-09-09 10:09:06.25114
31	15	3	Profit and Loss Account	t	t	2026-09-09 10:09:06.25114	2026-09-09 10:09:06.25114
32	15	4	Cash Account	f	t	2026-09-09 10:09:06.25114	2026-09-09 10:09:06.25114
33	16	1	Office Rent	t	t	2026-09-09 10:09:06.288966	2026-09-09 10:09:06.288966
34	16	2	Office Salaries	t	t	2026-09-09 10:09:06.288966	2026-09-09 10:09:06.288966
35	16	3	Advertising	t	t	2026-09-09 10:09:06.288966	2026-09-09 10:09:06.288966
36	16	4	Carriage Inward	f	t	2026-09-09 10:09:06.288966	2026-09-09 10:09:06.288966
37	17	1	Cash	t	t	2026-09-09 10:09:06.323802	2026-09-09 10:09:06.323802
38	17	2	Debtors	t	t	2026-09-09 10:09:06.323802	2026-09-09 10:09:06.323802
39	17	3	Furniture	t	t	2026-09-09 10:09:06.323802	2026-09-09 10:09:06.323802
40	17	4	Creditors	f	t	2026-09-09 10:09:06.323802	2026-09-09 10:09:06.323802
41	23	1	Purchases A/c Dr. ₹20,000 → To Cash A/c ₹20,000	t	t	2026-09-09 12:42:13.665406	2026-09-09 12:42:13.665406
42	23	2	Cash A/c Dr. ₹20,000 → To Purchases A/c ₹20,000	f	t	2026-09-09 12:42:13.665406	2026-09-09 12:42:13.665406
43	23	3	Sales A/c Dr. ₹20,000 → To Cash A/c ₹20,000	f	t	2026-09-09 12:42:13.665406	2026-09-09 12:42:13.665406
44	23	4	Purchases A/c Dr. ₹20,000 → To Sales A/c ₹20,000	f	t	2026-09-09 12:42:13.665406	2026-09-09 12:42:13.665406
45	24	1	Sales A/c Dr. → To Cash A/c	f	t	2026-09-09 12:52:16.931663	2026-09-09 12:52:16.931663
46	24	2	Cash A/c Dr. → To Sales A/c	t	t	2026-09-09 12:52:16.931663	2026-09-09 12:52:16.931663
47	24	3	Purchases A/c Dr. → To Cash A/c	f	t	2026-09-09 12:52:16.931663	2026-09-09 12:52:16.931663
48	24	4	Cash A/c Dr. → To Purchases A/c	f	t	2026-09-09 12:52:16.931663	2026-09-09 12:52:16.931663
49	25	1	Purchases A/c Dr. → To Cash A/c	f	t	2026-09-09 12:52:17.114659	2026-09-09 12:52:17.114659
50	25	2	Furniture A/c Dr. → To Cash A/c	t	t	2026-09-09 12:52:17.114659	2026-09-09 12:52:17.114659
51	25	3	Cash A/c Dr. → To Furniture A/c	f	t	2026-09-09 12:52:17.114659	2026-09-09 12:52:17.114659
52	25	4	Furniture A/c Dr. → To Purchases A/c	f	t	2026-09-09 12:52:17.114659	2026-09-09 12:52:17.114659
53	26	1	Cash A/c Dr. → To Rent A/c	f	t	2026-09-09 12:52:17.157437	2026-09-09 12:52:17.157437
54	26	2	Rent A/c Dr. → To Cash A/c	t	t	2026-09-09 12:52:17.157437	2026-09-09 12:52:17.157437
55	26	3	Rent A/c Dr. → To Capital A/c	f	t	2026-09-09 12:52:17.157437	2026-09-09 12:52:17.157437
56	26	4	Capital A/c Dr. → To Rent A/c	f	t	2026-09-09 12:52:17.157437	2026-09-09 12:52:17.157437
57	27	1	Cash A/c is debited	t	t	2026-09-09 12:52:17.195944	2026-09-09 12:52:17.195944
58	27	2	Capital A/c is credited	t	t	2026-09-09 12:52:17.195944	2026-09-09 12:52:17.195944
59	27	3	Cash A/c is credited	f	t	2026-09-09 12:52:17.195944	2026-09-09 12:52:17.195944
60	27	4	Capital A/c is debited	f	t	2026-09-09 12:52:17.195944	2026-09-09 12:52:17.195944
61	28	1	Ravi A/c Dr. → To Purchases A/c	f	t	2026-09-09 12:52:17.240884	2026-09-09 12:52:17.240884
62	28	2	Purchases A/c Dr. → To Ravi A/c	t	t	2026-09-09 12:52:17.240884	2026-09-09 12:52:17.240884
63	28	3	Cash A/c Dr. → To Ravi A/c	f	t	2026-09-09 12:52:17.240884	2026-09-09 12:52:17.240884
64	28	4	Ravi A/c Dr. → To Cash A/c	f	t	2026-09-09 12:52:17.240884	2026-09-09 12:52:17.240884
65	29	1	Sales A/c Dr. → To Ramesh A/c	f	t	2026-09-09 12:52:17.314386	2026-09-09 12:52:17.314386
66	29	2	Ramesh A/c Dr. → To Sales A/c	t	t	2026-09-09 12:52:17.314386	2026-09-09 12:52:17.314386
67	29	3	Cash A/c Dr. → To Sales A/c	f	t	2026-09-09 12:52:17.314386	2026-09-09 12:52:17.314386
68	29	4	Ramesh A/c Dr. → To Cash A/c	f	t	2026-09-09 12:52:17.314386	2026-09-09 12:52:17.314386
69	30	1	Salaries A/c is debited	t	t	2026-09-09 12:52:17.353612	2026-09-09 12:52:17.353612
70	30	2	Bank A/c is credited	t	t	2026-09-09 12:52:17.353612	2026-09-09 12:52:17.353612
71	30	3	Salaries A/c is credited	f	t	2026-09-09 12:52:17.353612	2026-09-09 12:52:17.353612
72	30	4	Bank A/c is debited	f	t	2026-09-09 12:52:17.353612	2026-09-09 12:52:17.353612
73	31	1	Arun A/c Dr. → To Cash A/c	f	t	2026-09-09 12:52:17.387833	2026-09-09 12:52:17.387833
74	31	2	Cash A/c Dr. → To Arun A/c	t	t	2026-09-09 12:52:17.387833	2026-09-09 12:52:17.387833
75	31	3	Sales A/c Dr. → To Arun A/c	f	t	2026-09-09 12:52:17.387833	2026-09-09 12:52:17.387833
76	31	4	Arun A/c Dr. → To Sales A/c	f	t	2026-09-09 12:52:17.387833	2026-09-09 12:52:17.387833
77	32	1	Drawings A/c is debited	t	t	2026-09-09 12:52:17.414617	2026-09-09 12:52:17.414617
78	32	2	Cash A/c is credited	t	t	2026-09-09 12:52:17.414617	2026-09-09 12:52:17.414617
79	32	3	Capital A/c is debited	f	t	2026-09-09 12:52:17.414617	2026-09-09 12:52:17.414617
80	32	4	Cash A/c is debited	f	t	2026-09-09 12:52:17.414617	2026-09-09 12:52:17.414617
\.


--
-- TOC entry 5447 (class 0 OID 30226)
-- Dependencies: 247
-- Data for Name: mcq_questions; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.mcq_questions (question_id, marks, active_row, created_at, updated_at, question_type_id) FROM stdin;
8	1	t	2026-09-08 20:39:51.07186	2026-09-08 20:39:51.07186	4
9	1	t	2026-09-09 10:09:05.818697	2026-09-09 10:09:05.818697	4
10	1	t	2026-09-09 10:09:06.062918	2026-09-09 10:09:06.062918	5
11	1	t	2026-09-09 10:09:06.106078	2026-09-09 10:09:06.106078	4
12	1	t	2026-09-09 10:09:06.14873	2026-09-09 10:09:06.14873	5
13	1	t	2026-09-09 10:09:06.189382	2026-09-09 10:09:06.189382	4
14	1	t	2026-09-09 10:09:06.221794	2026-09-09 10:09:06.221794	5
15	1	t	2026-09-09 10:09:06.25114	2026-09-09 10:09:06.25114	4
16	1	t	2026-09-09 10:09:06.288966	2026-09-09 10:09:06.288966	5
17	1	t	2026-09-09 10:09:06.323802	2026-09-09 10:09:06.323802	5
23	1	t	2026-09-09 12:42:13.665406	2026-09-09 12:42:13.665406	4
24	1	t	2026-09-09 12:52:16.931663	2026-09-09 12:52:16.931663	4
25	1	t	2026-09-09 12:52:17.114659	2026-09-09 12:52:17.114659	4
26	1	t	2026-09-09 12:52:17.157437	2026-09-09 12:52:17.157437	4
27	1	t	2026-09-09 12:52:17.195944	2026-09-09 12:52:17.195944	5
28	1	t	2026-09-09 12:52:17.240884	2026-09-09 12:52:17.240884	4
29	1	t	2026-09-09 12:52:17.314386	2026-09-09 12:52:17.314386	4
30	1	t	2026-09-09 12:52:17.353612	2026-09-09 12:52:17.353612	5
31	1	t	2026-09-09 12:52:17.387833	2026-09-09 12:52:17.387833	4
32	1	t	2026-09-09 12:52:17.414617	2026-09-09 12:52:17.414617	5
\.


--
-- TOC entry 5448 (class 0 OID 30237)
-- Dependencies: 248
-- Data for Name: plan_courses; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.plan_courses (plan_course_id, plan_id, course_id) FROM stdin;
\.


--
-- TOC entry 5432 (class 0 OID 30134)
-- Dependencies: 232
-- Data for Name: question_answers; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.question_answers (answer_id, user_id, question_id, table_name_id, header_id, attribute_id, arithmetic, amount, active_row, created_at, row_status, updated_at, condition_id, total_answers, pair_attribute_id, option_id, answer_text) FROM stdin;
1	2	8	\N	\N	\N	\N	\N	t	2026-09-08 21:05:30.930986	1	2026-09-08 21:05:30.930986	\N	\N	\N	1	Trading Account
\.


--
-- TOC entry 5437 (class 0 OID 30160)
-- Dependencies: 237
-- Data for Name: question_attributes; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.question_attributes (question_attribute_id, question_id, header_id, attribute_id, transaction_date, amount, amount2, note, active_row, created_at, updated_at) FROM stdin;
13	3	1	1	\N	2000.00	\N	\N	t	2026-09-08 18:24:02.491733	2026-09-08 18:24:02.491733
14	3	1	2	\N	4500.00	\N	\N	t	2026-09-08 18:24:02.503258	2026-09-08 18:24:02.503258
15	3	1	3	\N	600.00	\N	\N	t	2026-09-08 18:24:02.505261	2026-09-08 18:24:02.505261
16	3	1	4	\N	200.00	\N	\N	t	2026-09-08 18:24:02.575957	2026-09-08 18:24:02.575957
17	3	1	5	\N	400.00	\N	\N	t	2026-09-08 18:24:02.580467	2026-09-08 18:24:02.580467
18	3	1	6	\N	700.00	\N	\N	t	2026-09-08 18:24:02.585476	2026-09-08 18:24:02.585476
19	3	1	7	\N	900.00	\N	\N	t	2026-09-08 18:24:02.588468	2026-09-08 18:24:02.588468
20	3	2	8	\N	19000.00	\N	\N	t	2026-09-08 18:24:02.592981	2026-09-08 18:24:02.592981
21	3	2	9	\N	300.00	\N	\N	t	2026-09-08 18:24:02.596979	2026-09-08 18:24:02.596979
22	4	1	1	\N	3000.00	\N	\N	t	2026-09-08 20:09:06.545962	2026-09-08 20:09:06.545962
23	4	1	2	\N	14000.00	\N	\N	t	2026-09-08 20:09:06.590046	2026-09-08 20:09:06.590046
24	4	1	10	\N	1600.00	\N	\N	t	2026-09-08 20:09:06.594023	2026-09-08 20:09:06.594023
25	4	1	4	\N	200.00	\N	\N	t	2026-09-08 20:09:06.597921	2026-09-08 20:09:06.597921
26	4	1	3	\N	900.00	\N	\N	t	2026-09-08 20:09:06.602746	2026-09-08 20:09:06.602746
27	4	1	11	\N	1500.00	\N	\N	t	2026-09-08 20:09:06.611757	2026-09-08 20:09:06.611757
28	4	1	12	\N	570.00	\N	\N	t	2026-09-08 20:09:06.615816	2026-09-08 20:09:06.615816
29	4	2	8	\N	13200.00	\N	\N	t	2026-09-08 20:09:06.6205	2026-09-08 20:09:06.6205
30	4	2	9	\N	3000.00	\N	\N	t	2026-09-08 20:09:06.623882	2026-09-08 20:09:06.623882
31	4	2	8	\N	15000.00	\N	\N	t	2026-09-08 20:09:06.636755	2026-09-08 20:09:06.636755
32	4	2	14	\N	300.00	\N	\N	t	2026-09-08 20:09:06.641865	2026-09-08 20:09:06.641865
33	5	1	1	\N	5200.00	\N	\N	t	2026-09-08 20:09:06.789684	2026-09-08 20:09:06.789684
34	5	1	2	\N	4700.00	\N	\N	t	2026-09-08 20:09:06.792956	2026-09-08 20:09:06.792956
35	5	1	15	\N	22950.00	\N	\N	t	2026-09-08 20:09:06.795131	2026-09-08 20:09:06.795131
36	5	1	4	\N	800.00	\N	\N	t	2026-09-08 20:09:06.79718	2026-09-08 20:09:06.79718
37	5	1	6	\N	3800.00	\N	\N	t	2026-09-08 20:09:06.802685	2026-09-08 20:09:06.802685
38	5	1	11	\N	2150.00	\N	\N	t	2026-09-08 20:09:06.806087	2026-09-08 20:09:06.806087
39	5	1	16	\N	1500.00	\N	\N	t	2026-09-08 20:09:06.808786	2026-09-08 20:09:06.808786
40	5	2	17	\N	18000.00	\N	\N	t	2026-09-08 20:09:06.811217	2026-09-08 20:09:06.811217
41	5	2	18	\N	3000.00	\N	\N	t	2026-09-08 20:09:06.81405	2026-09-08 20:09:06.81405
42	5	2	8	\N	16000.00	\N	\N	t	2026-09-08 20:09:06.824123	2026-09-08 20:09:06.824123
43	5	2	19	\N	600.00	\N	\N	t	2026-09-08 20:09:06.826601	2026-09-08 20:09:06.826601
44	5	2	21	\N	2100.00	\N	\N	t	2026-09-08 20:09:06.828874	2026-09-08 20:09:06.828874
45	6	1	1	\N	5200.00	\N	\N	t	2026-09-08 20:09:06.870624	2026-09-08 20:09:06.870624
46	6	1	2	\N	4700.00	\N	\N	t	2026-09-08 20:09:06.872025	2026-09-08 20:09:06.872025
47	6	1	3	\N	800.00	\N	\N	t	2026-09-08 20:09:06.874471	2026-09-08 20:09:06.874471
48	6	1	4	\N	800.00	\N	\N	t	2026-09-08 20:09:06.879326	2026-09-08 20:09:06.879326
49	6	1	22	\N	1500.00	\N	\N	t	2026-09-08 20:09:06.885046	2026-09-08 20:09:06.885046
50	6	1	23	\N	3500.00	\N	\N	t	2026-09-08 20:09:06.88896	2026-09-08 20:09:06.88896
51	6	1	24	\N	2800.00	\N	\N	t	2026-09-08 20:09:06.895021	2026-09-08 20:09:06.895021
52	6	1	25	\N	3200.00	\N	\N	t	2026-09-08 20:09:06.896445	2026-09-08 20:09:06.897455
53	6	1	26	\N	15000.00	\N	\N	t	2026-09-08 20:09:06.898488	2026-09-08 20:09:06.898488
54	6	1	27	\N	6800.00	\N	\N	t	2026-09-08 20:09:06.922279	2026-09-08 20:09:06.922279
55	6	1	28	\N	1200.00	\N	\N	t	2026-09-08 20:09:06.926037	2026-09-08 20:09:06.926037
56	6	2	17	\N	18500.00	\N	\N	t	2026-09-08 20:09:06.932042	2026-09-08 20:09:06.932042
57	6	2	18	\N	3000.00	\N	\N	t	2026-09-08 20:09:06.939395	2026-09-08 20:09:06.939395
58	6	2	8	\N	13000.00	\N	\N	t	2026-09-08 20:09:06.942317	2026-09-08 20:09:06.942317
59	6	2	9	\N	600.00	\N	\N	t	2026-09-08 20:09:06.945095	2026-09-08 20:09:06.945095
60	6	2	29	\N	9000.00	\N	\N	t	2026-09-08 20:09:06.947892	2026-09-08 20:09:06.947892
61	6	2	30	\N	1400.00	\N	\N	t	2026-09-08 20:09:06.948904	2026-09-08 20:09:06.948904
62	7	1	1	\N	5200.00	\N	\N	t	2026-09-08 20:09:06.980579	2026-09-08 20:09:06.980579
63	7	1	2	\N	4700.00	\N	\N	t	2026-09-08 20:09:06.983107	2026-09-08 20:09:06.983107
64	7	1	3	\N	800.00	\N	\N	t	2026-09-08 20:09:06.986221	2026-09-08 20:09:06.986221
65	7	1	4	\N	800.00	\N	\N	t	2026-09-08 20:09:06.988206	2026-09-08 20:09:06.988206
66	7	1	12	\N	2150.00	\N	\N	t	2026-09-08 20:09:06.990207	2026-09-08 20:09:06.990207
67	7	1	31	\N	1850.00	\N	\N	t	2026-09-08 20:09:06.993275	2026-09-08 20:09:06.993275
68	7	1	32	\N	1500.00	\N	\N	t	2026-09-08 20:09:06.996288	2026-09-08 20:09:06.996288
69	7	1	33	\N	1700.00	\N	\N	t	2026-09-08 20:09:07.000873	2026-09-08 20:09:07.002219
70	7	1	34	\N	2500.00	\N	\N	t	2026-09-08 20:09:07.004978	2026-09-08 20:09:07.004978
71	7	1	15	\N	3500.00	\N	\N	t	2026-09-08 20:09:07.006358	2026-09-08 20:09:07.006358
72	7	1	35	\N	6000.00	\N	\N	t	2026-09-08 20:09:07.010267	2026-09-08 20:09:07.012392
73	7	2	17	\N	10000.00	\N	\N	t	2026-09-08 20:09:07.01513	2026-09-08 20:09:07.01513
74	7	2	18	\N	3000.00	\N	\N	t	2026-09-08 20:09:07.016697	2026-09-08 20:09:07.016697
75	7	2	8	\N	13000.00	\N	\N	t	2026-09-08 20:09:07.018861	2026-09-08 20:09:07.018861
76	7	2	9	\N	600.00	\N	\N	t	2026-09-08 20:09:07.020853	2026-09-08 20:09:07.020853
77	7	2	14	\N	1400.00	\N	\N	t	2026-09-08 20:09:07.022853	2026-09-08 20:09:07.022853
78	7	2	36	\N	2100.00	\N	\N	t	2026-09-08 20:09:07.023854	2026-09-08 20:09:07.023854
79	7	2	37	\N	600.00	\N	\N	t	2026-09-08 20:09:07.02536	2026-09-08 20:09:07.02536
80	18	3	39	\N	2000.00	\N	\N	t	2026-09-09 11:54:56.826779	2026-09-09 11:54:56.826779
81	18	3	40	\N	1500.00	\N	\N	t	2026-09-09 11:54:57.077496	2026-09-09 11:54:57.077496
82	18	3	41	\N	6800.00	\N	\N	t	2026-09-09 11:54:57.080497	2026-09-09 11:54:57.080497
83	18	3	42	\N	9500.00	\N	\N	t	2026-09-09 11:54:57.097013	2026-09-09 11:54:57.097013
84	18	3	43	\N	8500.00	\N	\N	t	2026-09-09 11:54:57.136534	2026-09-09 11:54:57.136534
85	19	3	44	\N	5700.00	\N	\N	t	2026-09-09 12:05:42.540483	2026-09-09 12:05:42.540483
86	19	3	45	\N	8200.00	\N	\N	t	2026-09-09 12:05:42.553009	2026-09-09 12:05:42.553009
87	19	3	46	\N	4600.00	\N	\N	t	2026-09-09 12:05:42.557086	2026-09-09 12:05:42.557086
88	19	3	47	\N	7100.00	\N	\N	t	2026-09-09 12:05:42.560099	2026-09-09 12:05:42.560099
89	19	3	48	\N	8200.00	\N	\N	t	2026-09-09 12:05:42.564585	2026-09-09 12:05:42.564585
90	20	3	49	\N	2500.00	\N	\N	t	2026-09-09 12:05:42.676292	2026-09-09 12:05:42.676292
91	20	3	50	\N	6400.00	\N	\N	t	2026-09-09 12:05:42.706661	2026-09-09 12:05:42.706661
92	20	3	51	\N	8200.00	\N	\N	t	2026-09-09 12:05:42.710664	2026-09-09 12:05:42.710664
93	20	3	52	\N	1400.00	\N	\N	t	2026-09-09 12:05:42.733349	2026-09-09 12:05:42.733349
94	20	3	53	\N	5800.00	\N	\N	t	2026-09-09 12:05:42.736714	2026-09-09 12:05:42.736714
95	20	3	54	\N	9600.00	\N	\N	t	2026-09-09 12:05:42.749637	2026-09-09 12:05:42.749637
96	21	3	55	\N	3500.00	\N	\N	t	2026-09-09 12:05:42.780268	2026-09-09 12:05:42.780268
97	21	3	56	\N	6800.00	\N	\N	t	2026-09-09 12:05:42.782371	2026-09-09 12:05:42.782371
98	21	3	57	\N	1200.00	\N	\N	t	2026-09-09 12:05:42.803115	2026-09-09 12:05:42.803115
99	21	3	58	\N	9400.00	\N	\N	t	2026-09-09 12:05:42.809096	2026-09-09 12:05:42.809096
100	21	3	59	\N	1500.00	\N	\N	t	2026-09-09 12:05:42.812556	2026-09-09 12:05:42.812556
101	22	3	60	\N	7200.00	\N	\N	t	2026-09-09 12:05:42.841583	2026-09-09 12:05:42.841583
102	22	3	61	\N	3800.00	\N	\N	t	2026-09-09 12:05:42.843594	2026-09-09 12:05:42.843594
103	22	3	62	\N	6400.00	\N	\N	t	2026-09-09 12:05:42.846289	2026-09-09 12:05:42.846289
104	22	3	63	\N	5900.00	\N	\N	t	2026-09-09 12:05:42.848805	2026-09-09 12:05:42.848805
105	22	3	64	\N	9200.00	\N	\N	t	2026-09-09 12:05:42.852009	2026-09-09 12:05:42.852009
106	33	3	65	\N	500.00	\N	\N	t	2026-09-09 19:14:33.936699	2026-09-09 19:14:33.936699
107	33	3	66	\N	400.00	\N	\N	t	2026-09-09 19:14:34.001479	2026-09-09 19:14:34.001479
108	33	3	67	\N	500.00	\N	\N	t	2026-09-09 19:14:34.004476	2026-09-09 19:14:34.004476
109	33	3	68	\N	200.00	\N	\N	t	2026-09-09 19:14:34.007987	2026-09-09 19:14:34.007987
110	33	3	69	\N	\N	\N	\N	t	2026-09-09 19:14:34.010028	2026-09-09 19:14:34.010028
111	34	3	70	\N	650.00	\N	\N	t	2026-09-09 19:22:12.514918	2026-09-09 19:22:12.514918
112	34	3	71	\N	1450.00	\N	\N	t	2026-09-09 19:22:12.520872	2026-09-09 19:22:12.520872
113	34	3	72	\N	600.00	\N	\N	t	2026-09-09 19:22:12.521886	2026-09-09 19:22:12.521886
114	34	3	73	\N	1500.00	\N	\N	t	2026-09-09 19:22:12.525405	2026-09-09 19:22:12.525405
115	34	3	74	\N	850.00	\N	\N	t	2026-09-09 19:22:12.527404	2026-09-09 19:22:12.527404
116	35	3	75	\N	20.00	\N	\N	t	2026-09-09 19:22:12.546367	2026-09-09 19:22:12.546367
117	35	3	76	\N	2000.00	\N	\N	t	2026-09-09 19:22:12.549409	2026-09-09 19:22:12.549409
118	35	3	77	\N	27.00	\N	\N	t	2026-09-09 19:22:12.55037	2026-09-09 19:22:12.55037
119	35	3	78	\N	35.00	\N	\N	t	2026-09-09 19:22:12.552365	2026-09-09 19:22:12.552365
120	35	3	79	\N	90.00	\N	\N	t	2026-09-09 19:22:12.553368	2026-09-09 19:22:12.553368
121	36	3	80	\N	75.00	\N	\N	t	2026-09-09 19:22:12.574301	2026-09-09 19:22:12.574301
122	36	3	81	\N	650.00	\N	\N	t	2026-09-09 19:22:12.575805	2026-09-09 19:22:12.575805
123	36	3	82	\N	225.00	\N	\N	t	2026-09-09 19:22:12.577813	2026-09-09 19:22:12.577813
124	36	3	83	\N	150.00	\N	\N	t	2026-09-09 19:22:12.579811	2026-09-09 19:22:12.579811
125	37	3	84	\N	700.00	\N	\N	t	2026-09-09 19:22:12.595628	2026-09-09 19:22:12.595628
126	37	3	85	\N	1600.00	\N	\N	t	2026-09-09 19:22:12.598377	2026-09-09 19:22:12.598377
127	37	3	86	\N	240.00	\N	\N	t	2026-09-09 19:22:12.600381	2026-09-09 19:22:12.600381
128	37	3	87	\N	2100.00	\N	\N	t	2026-09-09 19:22:12.601383	2026-09-09 19:22:12.601383
129	38	3	88	\N	430.00	\N	\N	t	2026-09-09 21:45:08.503397	2026-09-09 21:45:08.503397
130	38	3	89	\N	200.00	\N	\N	t	2026-09-09 21:45:08.523438	2026-09-09 21:45:08.523438
131	38	3	90	\N	3000.00	\N	\N	t	2026-09-09 21:45:08.525437	2026-09-09 21:45:08.525437
132	38	3	91	\N	2400.00	\N	\N	t	2026-09-09 21:45:08.528851	2026-09-09 21:45:08.528851
133	39	3	92	\N	2000.00	\N	\N	t	2026-09-09 21:45:08.581787	2026-09-09 21:45:08.581787
134	39	3	93	\N	1500.00	\N	\N	t	2026-09-09 21:45:08.584263	2026-09-09 21:45:08.584263
135	39	3	94	\N	200.00	\N	\N	t	2026-09-09 21:45:08.589583	2026-09-09 21:45:08.589583
136	39	3	95	\N	3500.00	\N	\N	t	2026-09-09 21:45:08.591826	2026-09-09 21:45:08.591826
137	40	3	96	\N	1500.00	\N	\N	t	2026-09-09 21:45:08.612637	2026-09-09 21:45:08.612637
138	40	3	97	\N	500.00	\N	\N	t	2026-09-09 21:45:08.614654	2026-09-09 21:45:08.614654
139	40	3	98	\N	5000.00	\N	\N	t	2026-09-09 21:45:08.619096	2026-09-09 21:45:08.619096
140	40	3	99	\N	100.00	\N	\N	t	2026-09-09 21:45:08.622605	2026-09-09 21:45:08.622605
141	41	3	100	\N	2000.00	\N	\N	t	2026-09-09 21:45:08.648255	2026-09-09 21:45:08.648255
142	41	3	101	\N	5000.00	\N	\N	t	2026-09-09 21:45:08.651268	2026-09-09 21:45:08.651268
143	41	3	102	\N	1500.00	\N	\N	t	2026-09-09 21:45:08.655805	2026-09-09 21:45:08.655805
144	41	3	103	\N	850.00	\N	\N	t	2026-09-09 21:45:08.657807	2026-09-09 21:45:08.657807
145	41	3	104	\N	500.00	\N	\N	t	2026-09-09 21:45:08.661892	2026-09-09 21:45:08.661892
146	42	3	105	\N	4000.00	\N	\N	t	2026-09-09 21:45:08.697901	2026-09-09 21:45:08.697901
147	42	3	106	\N	235.00	\N	\N	t	2026-09-09 21:45:08.699418	2026-09-09 21:45:08.699418
148	42	3	107	\N	1000.00	\N	\N	t	2026-09-09 21:45:08.70172	2026-09-09 21:45:08.70172
149	42	3	108	\N	1200.00	\N	\N	t	2026-09-09 21:45:08.706048	2026-09-09 21:45:08.706048
150	42	3	109	\N	2000.00	\N	\N	t	2026-09-09 21:45:08.709483	2026-09-09 21:45:08.709483
\.


--
-- TOC entry 5452 (class 0 OID 30258)
-- Dependencies: 252
-- Data for Name: question_type; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.question_type (question_type_id, question_type, created_at) FROM stdin;
2	Journal	2026-09-08 18:11:08.355893
3	DropDown	2026-09-08 18:11:08.355893
4	MCQ Single Choice	2026-09-08 18:11:08.355893
5	MCQ Multiple Choice	2026-09-08 18:11:08.355893
1	Drag And Drop	2026-09-08 18:11:08.355893
\.


--
-- TOC entry 5442 (class 0 OID 30199)
-- Dependencies: 242
-- Data for Name: questions; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.questions (question_id, chapter_id, topic_id, question_text, active_row, created_at, updated_at, course_id, question_type_id, subject_id) FROM stdin;
3	1	1	Prepare Trading Account from the following information	t	2026-09-08 18:24:02.441532	2026-09-08 18:24:02.444035	1	1	3
4	1	1	Prepare Trading Account and Profit & Loss Account from the following information	t	2026-09-08 20:09:06.411293	2026-09-08 20:09:06.41366	1	1	3
5	1	1	Prepare Final accounts	t	2026-09-08 20:09:06.783628	2026-09-08 20:09:06.783628	1	1	3
6	1	1	Prepare Final accounts	t	2026-09-08 20:09:06.864781	2026-09-08 20:09:06.864781	1	1	3
7	1	1	Prepare Final accounts	t	2026-09-08 20:09:06.972473	2026-09-08 20:09:06.972473	1	1	3
8	1	9	Which account is prepared to determine gross profit or gross loss?	t	2026-09-08 20:39:51.141194	2026-09-08 20:39:51.141194	1	4	3
9	1	9	Opening stock is shown on which side of the Trading Account?	t	2026-09-09 10:09:05.866695	2026-09-09 10:09:05.866695	1	4	3
10	1	9	Which items are shown on the debit side of the Trading Account?	t	2026-09-09 10:09:06.071151	2026-09-09 10:09:06.071151	1	5	3
11	1	9	Gross profit is calculated as:	t	2026-09-09 10:09:06.113722	2026-09-09 10:09:06.113722	1	4	3
12	1	9	Which of the following are direct expenses?	t	2026-09-09 10:09:06.158024	2026-09-09 10:09:06.158024	1	5	3
13	1	9	Gross profit is transferred to which account?	t	2026-09-09 10:09:06.194168	2026-09-09 10:09:06.194168	1	4	3
14	1	9	Which items appear on the credit side of the Trading Account?	t	2026-09-09 10:09:06.225977	2026-09-09 10:09:06.225977	1	5	3
15	1	9	Which account is prepared to determine net profit or net loss?	t	2026-09-09 10:09:06.25698	2026-09-09 10:09:06.25698	1	4	3
16	1	9	Which of the following are indirect expenses?	t	2026-09-09 10:09:06.293873	2026-09-09 10:09:06.293873	1	5	3
17	1	9	Which of the following are assets shown in the Balance Sheet?	t	2026-09-09 10:09:06.32863	2026-09-09 10:09:06.32863	1	5	3
18	2	4	Transactions involves only Real Accounts	t	2026-09-09 11:54:56.285359	2026-09-09 11:54:56.285359	1	2	3
19	2	4	Transactions involves only Personal Accounts	t	2026-09-09 12:05:42.459407	2026-09-09 12:05:42.464969	1	2	3
20	2	4	Transactions involves only Real and Nominal Accounts	t	2026-09-09 12:05:42.667325	2026-09-09 12:05:42.667325	1	2	3
21	2	4	Transactions involves only Nominal and Personal Accounts	t	2026-09-09 12:05:42.774748	2026-09-09 12:05:42.774748	1	2	3
22	2	4	Transactions involves only Real and Personal Accounts	t	2026-09-09 12:05:42.837824	2026-09-09 12:05:42.837824	1	2	3
23	2	10	Goods purchased for cash ₹20,000. What is the correct journal entry?	t	2026-09-09 12:42:13.722292	2026-09-09 12:42:13.724113	1	4	3
24	2	10	Goods sold for cash ₹15,000. What is the correct journal entry?	t	2026-09-09 12:52:17.015044	2026-09-09 12:52:17.016044	1	4	3
25	2	10	Furniture purchased for cash ₹30,000. What is the correct journal entry?	t	2026-09-09 12:52:17.120166	2026-09-09 12:52:17.120166	1	4	3
26	2	10	Paid office rent ₹5,000 in cash. What is the correct entry?	t	2026-09-09 12:52:17.163369	2026-09-09 12:52:17.163369	1	4	3
27	2	10	Started business with cash ₹1,00,000. Which statements are correct?	t	2026-09-09 12:52:17.202468	2026-09-09 12:52:17.202468	1	5	3
28	2	10	Purchased goods on credit from Ravi for ₹25,000. What is the correct entry?	t	2026-09-09 12:52:17.245259	2026-09-09 12:52:17.245259	1	4	3
29	2	10	Sold goods on credit to Ramesh for ₹18,000. What is the correct entry?	t	2026-09-09 12:52:17.317674	2026-09-09 12:52:17.317674	1	4	3
30	2	10	Paid salaries ₹12,000 by bank. Which statements are correct?	t	2026-09-09 12:52:17.357039	2026-09-09 12:52:17.357039	1	5	3
31	2	10	Received ₹10,000 cash from a debtor, Arun. What is the correct entry?	t	2026-09-09 12:52:17.391761	2026-09-09 12:52:17.391761	1	4	3
32	2	10	Owner withdrew ₹5,000 cash for personal use. Which statements are correct?	t	2026-09-09 12:52:17.418119	2026-09-09 12:52:17.418119	1	5	3
33	3	5	With Suspense Account : undercast of Subsidiary Books	t	2026-09-09 19:14:33.907694	2026-09-09 19:14:33.907694	1	3	3
34	3	5	With Suspense Account : overcast of Subsidiary Books	t	2026-09-09 19:22:12.503312	2026-09-09 19:22:12.503312	1	3	3
35	3	5	With Suspense Account : Wrong Amount entered in the account	t	2026-09-09 19:22:12.542851	2026-09-09 19:22:12.542851	1	3	3
36	3	5	With Suspense Account : Error of Partial Omission	t	2026-09-09 19:22:12.569689	2026-09-09 19:22:12.569689	1	3	3
37	3	5	With Suspense Account : Posted wrongly on Debit side of an account	t	2026-09-09 19:22:12.591628	2026-09-09 19:22:12.591628	1	3	3
38	3	5	With Suspense Account : Posted wrongly on Credit side of an account	t	2026-09-09 21:45:08.464358	2026-09-09 21:45:08.464358	1	3	3
39	3	5	Without Suspense Account : Posted in the Wrong account name on credit side	t	2026-09-09 21:45:08.576591	2026-09-09 21:45:08.576591	1	3	3
40	3	5	Without Suspense Account : Posted in the Wrong account name on Debit side	t	2026-09-09 21:45:08.608878	2026-09-09 21:45:08.608878	1	3	3
41	3	5	Without Suspense Account : Principle error	t	2026-09-09 21:45:08.64125	2026-09-09 21:45:08.64125	1	3	3
42	3	5	Without Suspense Account : Error of Complete omission	t	2026-09-09 21:45:08.693898	2026-09-09 21:45:08.693898	1	3	3
\.


--
-- TOC entry 5455 (class 0 OID 30266)
-- Dependencies: 255
-- Data for Name: roles; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.roles (role_id, role_name, created_at, updated_at) FROM stdin;
1	SUPER_ADMIN	2026-09-08 12:31:03.560609	2026-09-08 12:31:03.560609
2	COLLEGE_ADMIN	2026-09-08 12:31:03.560609	2026-09-08 12:31:03.560609
3	BRANCH_ADMIN	2026-09-08 12:31:03.560609	2026-09-08 12:31:03.560609
4	STUDENT	2026-09-08 12:31:03.560609	2026-09-08 12:31:03.560609
5	GUEST	2026-09-08 12:31:03.560609	2026-09-08 12:31:03.560609
\.


--
-- TOC entry 5457 (class 0 OID 30274)
-- Dependencies: 257
-- Data for Name: rule_engines; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.rule_engines (rule_engine_id, chapter_id, pair_attribute_id, relationship_name, pair_order, arithmetic1, table1_id, header1_id, amount_position1, information1, arithmetic2, table2_id, header2_id, amount_position2, information2, arithmetic3, table3_id, header3_id, amount_position3, information3, arithmetic4, table4_id, header4_id, amount_position4, information4, active_row, created_at, row_status, updated_at, attribute_id) FROM stdin;
1	1	1	1to1	1	add	1	1	1	opening stock is used in trading or manufacturing. It is Direct expense. Add on debit side of Trading a/c	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-08 17:34:10.770863	1	2026-09-08 17:34:10.770863	1
2	1	2	1to1	1	add	1	1	1	Purchases is used for resale or in manufacturing.<br/>it is direct expense.<br/>Add on debit side of Trading A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-08 17:40:41.157656	1	2026-09-08 17:40:41.157656	2
3	1	3	1to1	1	add	1	1	1	wages is expenses on purchases or manufacturing.<br/>it is direct expense.<br/>Add on debit side of Trading A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f	2026-09-08 17:43:14.305128	1	2026-09-08 17:43:14.305128	3
4	1	4	1to1	2	less	1	2	1	Sales  returns means goods returned by Customer.<br/>It should be decreased from sales amount.<br/>Subtract from sales on credit side of Trading A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-08 17:48:09.48864	1	2026-09-08 17:48:09.48864	4
5	1	5	1to1	1	add	1	1	1	Carriage is expenses on purchases or manufacturing.<br/>it is direct expense.<br/>Add on debit side of Trading A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-08 17:49:21.422707	1	2026-09-08 17:49:21.422707	5
6	1	6	1to1	1	add	1	1	1	Coal and Gas is expense incurred in manufacturing process<br/>it is direct expense.<br/>Add on debit side of Trading A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-08 17:50:39.263868	1	2026-09-08 17:50:39.263868	6
7	1	7	1to1	1	add	1	1	1	Factory rent is an expense on manufacturing.<br/>it is direct expense.<br/>Add on debit side of Trading A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-08 17:52:53.245875	1	2026-09-08 17:52:53.245875	7
8	1	8	1to1	1	add	1	2	1	Sales is the income from sale of goods.<br/>it is from ordinary course of business.    <br/>Add on credit side of Trading A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-08 17:55:34.452345	1	2026-09-08 17:55:34.452345	8
9	1	9	1to1	2	less	1	1	0	Purchase returns is goods returned to suppliers .<br/>it is to be subtracted from purchases.<br/>less on debit side of Trading A/c	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-08 18:04:47.5032	1	2026-09-08 18:04:47.5032	9
10	1	10	1to1	1	add	3	1	1	Salaries expenses not directly related to purchases. <br/>it is indirect expense.<br/>Add on debit side of Profit & Loss A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-08 18:39:33.649743	1	2026-09-08 18:39:33.649743	10
11	1	11	1to1	1	add	3	1	1	Rent expenses not directly related to purchases. <br/>it is indirect expense.<br/>Add on debit side of Profit & Loss A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-08 18:41:33.846439	1	2026-09-08 18:41:33.846439	11
12	1	12	1to1	1	add	1	1	1	Import Duty incurred in manufacturing process<br/>it is direct expense.<br/>Add on debit side of Trading A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-08 18:42:53.47146	1	2026-09-08 18:42:53.47146	12
13	1	14	1to1	1	add	3	2	1	discount received/other than sales income/ other income/add on credit side of profit and loss a/c	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-08 18:54:29.529914	1	2026-09-08 18:54:29.529914	14
14	1	15	1to1	1	add	2	1	1	Plant & Machinery is meant for use in business <br/>it is Fixed asset.<br/>Add on Asset side of Balance Sheet.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-08 18:58:26.533598	1	2026-09-08 18:58:26.533598	15
15	1	38	1to1	1	add	1	1	1	Coal and water is expense incurred in manufacturing process<br/>it is direct expense.<br/>Add on debit side of Trading A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-08 19:01:01.601043	1	2026-09-08 19:01:01.601043	38
16	1	16	1to1	1	add	3	1	1	Interest on capital is the charges paid to owner for capital contribution.<br/>not related to purchases. It is indirect expense<br/>Add on debit side of Profit & Loss A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-08 19:02:42.986696	1	2026-09-08 19:02:42.986696	16
17	1	17	1to1	1	add	2	5	1	Capital is funds contributed by the owner.<br/>it is the internal liability for the business.<br/>Add on Liability side of Balance Sheet.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-08 19:10:44.791755	1	2026-09-08 19:10:44.791755	17
18	1	18	1to1	1	add	2	5	1	Creditors include supplier of goods & Lenders.<br/>it is the liability for the business.<br/>Add on Liability side of Balance Sheet.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-08 19:12:11.450785	1	2026-09-08 19:12:11.450785	18
19	1	2	1to1	1	less	1	1	1	Return outwards (Purchase returns) means goods returned to suppliers.<br/>it is to be subtracted from purchases.<br/>less on debit side of Trading A/c	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-08 19:13:41.942812	1	2026-09-08 19:13:41.942812	19
20	1	21	1to1	1	add	3	2	1	Dividend received from investments in shares.<br/>It is other than Sales income<br/>Add on credit side of Profit & Loss A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-08 19:16:26.809351	1	2026-09-08 19:16:26.809351	21
21	1	22	1to1	1	add	1	1	1	Fuel & Power is expense incurred in manufacturing process<br/>it is direct expense.<br/>Add on debit side of Trading A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-08 19:19:27.162112	1	2026-09-08 19:19:27.162112	22
22	1	23	1to1	1	add	1	1	1	Gas and water is expense incurred in manufacturing process<br/>it is direct expense.<br/>Add on debit side of Trading A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-08 19:21:13.341517	1	2026-09-08 19:21:13.341517	23
23	1	24	1to1	1	add	3	1	1	Office expense is not related to purchases.<br/>it is indirect expense<br/>Add on debit side of Profit & Loss A/c	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-08 19:22:28.689016	1	2026-09-08 19:22:28.689016	24
24	1	25	1to1	1	add	3	1	1	Postage expense is not related to purchases.<br/>it is indirect expense<br/>Add on debit side of Profit & Loss A/c	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-08 19:23:34.753447	1	2026-09-08 19:23:34.753447	25
25	1	26	1to1	1	add	2	4	1	Investment is meant for generating other income to the business.<br/>It is an asset<br/>Add on Asset side of Balance Sheet.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-08 19:24:54.701235	1	2026-09-08 19:24:54.701235	26
26	1	27	1to1	1	add	2	4	1	Land & Buildings are meant for use in business <br/>it is Fixed asset.<br/>Add on Asset side of Balance Sheet.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-08 19:25:54.334841	1	2026-09-08 19:25:54.334841	27
27	1	28	1to1	1	add	2	4	1	Good will is the reputation of the entity<br/>It is an intangible Asset<br/>Add on Asset side of Balance Sheet.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-08 19:27:34.78453	1	2026-09-08 19:27:34.78453	28
28	1	29	1to1	1	add	3	2	1	Interest on drawings is the amount received from the owner on the drawings made by him.<br/>It is other than Sales income<br/>Add on credit side of Profit & Loss A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-08 19:28:53.861618	1	2026-09-08 19:28:53.861618	29
29	1	30	1to1	1	add	3	2	1	Miscellaneous income means income received from other sources.<br/>It is other than Sales income<br/>Add on credit side of Profit & Loss A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-08 19:29:54.816354	1	2026-09-08 19:29:54.816354	30
30	1	31	1to1	1	add	1	1	1	Lighting is expense incurred in manufacturing process<br/>it is direct expense.<br/>Add on debit side of Trading A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-08 19:33:14.60061	1	2026-09-08 19:33:14.60061	31
31	1	32	1to1	1	add	1	1	1	Oil & fuel is the expenses used in manufacturing process.<br/>it is direct expense.<br/>Add on debit side of Trading A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-08 19:34:25.947374	1	2026-09-08 19:34:25.947374	32
32	1	33	1to1	1	add	3	1	1	Printing & stationery is not directly related to purchases<br/>it is indirect expense<br/>Add on debit side of Profit & Loss A/c	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-08 19:35:28.423783	1	2026-09-08 19:35:28.423783	33
33	1	34	1to1	1	add	3	1	1	Printing Charges is not directly related to purchases<br/>it is indirect expense<br/>Add on debit side of Profit & Loss A/c	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-08 19:38:35.699875	1	2026-09-08 19:38:35.699875	34
34	1	35	1to1	1	add	2	4	1	Rent paid in advance means rent paid more than actual.<br/>Receivable in future<br/>Add on Asset side of Balance Sheet.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-08 19:40:02.658213	1	2026-09-08 19:40:02.658213	35
35	1	36	1to1	1	add	3	2	1	Dividend on credit side means Dividend received from Investment in Shares.<br/>It is other than Sales income<br/>Add on credit side of Profit & Loss A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-08 19:42:02.704106	1	2026-09-08 19:42:02.704106	36
36	1	37	1to1	1	add	2	5	1	Outstanding rent means rent paid less than actual.<br/>It is payable in future so it is liability.<br/>Add on Liability side of Balance Sheet.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-08 19:43:13.447039	1	2026-09-08 19:43:13.447039	37
37	2	39	1to2	1	add	4	1	1	Traditional Approach<br/>Furniture A/c - Real - Furniture coming in - Dr<br/>Cash A/c - Real - Cash Going out - Cr<br/>Accounting equation Approach<br/>Furniture A/c - Asset - Furniture Increased - Dr<br/>Cash A/c - Asset - Cash Decreased - Cr	add	5	2	1	Traditional Approach<br/>Furniture A/c - Real - Furniture coming in - Dr<br/>Cash A/c - Real - Cash Going out - Cr<br/>Accounting equation Approach<br/>Furniture A/c - Asset - Furniture Increased - Dr<br/>Cash A/c - Asset - Cash Decreased - Cr	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 10:36:29.091515	1	2026-09-09 10:36:29.091515	39
38	2	40	1to2	1	add	6	1	1	Traditional Approach<br/>Machinery A/c - Real - Machinery coming in - Dr<br/>Cash A/c - Real - Cash Going out - Cr<br/>Accounting equation Approach<br/>Machinery A/c - Asset - Machinery Increased - Dr<br/>Cash A/c - Asset - Cash Decreased - Cr	add	5	2	1	Traditional Approach<br/>Machinery A/c - Real - Machinery coming in - Dr<br/>Cash A/c - Real - Cash Going out - Cr<br/>Accounting equation Approach<br/>Machinery A/c - Asset - Machinery Increased - Dr<br/>Cash A/c - Asset - Cash Decreased - Cr	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 10:38:23.233206	1	2026-09-09 10:38:23.233206	40
39	2	41	1to2	1	add	7	1	1	Traditional Approach<br/>Computer A/c - Real - Computer coming in - Dr<br/>Cash A/c - Real - Cash Going out - Cr<br/>Accounting equation Approach<br/>Computer A/c - Asset - Computer Increased - Dr<br/>Cash A/c - Asset - Cash Decreased - Cr	add	5	2	1	Traditional Approach<br/>Computer A/c - Real - Computer coming in - Dr<br/>Cash A/c - Real - Cash Going out - Cr<br/>Accounting equation Approach<br/>Computer A/c - Asset - Computer Increased - Dr<br/>Cash A/c - Asset - Cash Decreased - Cr	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 10:41:03.507634	1	2026-09-09 10:41:03.507634	41
40	2	42	1to2	1	add	5	1	1	Traditional Approach<br/>Cash A/c - Real - Cash coming in - Dr<br/>Machinery A/c - Real - Machinery Going out - Cr<br/>Accounting equation Approach<br/>Cash A/c - Asset - Cash Increased - Dr<br/>Machinery A/c - Asset - Machinery Decreased - Cr	add	6	2	1	Traditional Approach<br/>Cash A/c - Real - Cash coming in - Dr<br/>Machinery A/c - Real - Machinery Going out - Cr<br/>Accounting equation Approach<br/>Cash A/c - Asset - Cash Increased - Dr<br/>Machinery A/c - Asset - Machinery Decreased - Cr	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 10:43:13.004024	1	2026-09-09 10:43:13.004024	42
41	2	43	1to2	1	add	8	2	1	Traditional Approach<br/>Cash A/c - Real - Cash coming in - Dr<br/>Building A/c - Real - Building Going out - Cr<br/>Accounting equation Approach<br/>Cash A/c - Asset - Cash Increased - Dr<br/>Building A/c - Asset - Building Decreased - Cr	add	5	1	1	Traditional Approach<br/>Cash A/c - Real - Cash coming in - Dr<br/>Building A/c - Real - Building Going out - Cr<br/>Accounting equation Approach<br/>Cash A/c - Asset - Cash Increased - Dr<br/>Building A/c - Asset - Building Decreased - Cr	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 10:44:45.535996	1	2026-09-09 10:44:45.535996	43
42	2	44	1to2	1	add	9	1	1	Traditional Approach<br/>Bank A/c - Personal - Bank is the Receiver - Dr<br/>Capital A/c - Personal - Capital is the Giver - Cr<br/>Accounting equation Approach<br/>Bank A/c - Asset -  Increased - Dr<br/>Capital A/c - Capital -  Increased - Cr	add	10	2	1	Traditional Approach<br/>Bank A/c - Personal - Bank is the Receiver - Dr<br/>Capital A/c - Personal - Capital is the Giver - Cr<br/>Accounting equation Approach<br/>Bank A/c - Asset -  Increased - Dr<br/>Capital A/c - Capital -  Increased - Cr	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 10:46:25.089982	1	2026-09-09 10:46:25.089982	44
43	2	45	1to2	1	add	9	1	1	Traditional Approach<br/>Bank A/c - Personal - Bank is the Receiver - Dr<br/>Shankar A/c - Personal - Shankar is the Giver - Cr<br/>Accounting equation Approach<br/>Bank A/c - Asset -  Increased - Dr<br/>Shankar A/c - Asset -  Decreased - Cr	add	11	2	1	Traditional Approach<br/>Bank A/c - Personal - Bank is the Receiver - Dr<br/>Shankar A/c - Personal - Shankar is the Giver - Cr<br/>Accounting equation Approach<br/>Bank A/c - Asset -  Increased - Dr<br/>Shankar A/c - Asset -  Decreased - Cr	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 10:49:37.646135	1	2026-09-09 10:49:37.646135	45
44	2	46	1to2	1	add	12	1	1	Traditional Approach<br/>Vaishali A/c - Personal - Vaishali is the Receiver - Dr<br/>Bank A/c - Personal - Bank is the Giver - Cr<br/>Accounting equation Approach<br/>Vaishali A/c - Liability -  Decreased - Dr<br/>Bank A/c - Asset -  Decreased - Cr	add	9	2	1	Traditional Approach<br/>Vaishali A/c - Personal - Vaishali is the Receiver - Dr<br/>Bank A/c - Personal - Bank is the Giver - Cr<br/>Accounting equation Approach<br/>Vaishali A/c - Liability -  Decreased - Dr<br/>Bank A/c - Asset -  Decreased - Cr	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 10:56:35.136499	1	2026-09-09 10:56:35.136499	46
45	2	47	1to2	1	add	13	1	1	Traditional Approach<br/>Drawings A/c - Personal - Drawings is the Receiver - Dr<br/>Bank A/c - Personal - Bank is the Giver - Cr<br/>Accounting equation Approach<br/>Drawings A/c - Capital -  Decreased - Dr<br/>Bank A/c - Asset -  Decreased - Cr	add	9	2	1	Traditional Approach<br/>Drawings A/c - Personal - Drawings is the Receiver - Dr<br/>Bank A/c - Personal - Bank is the Giver - Cr<br/>Accounting equation Approach<br/>Drawings A/c - Capital -  Decreased - Dr<br/>Bank A/c - Asset -  Decreased - Cr	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 11:03:17.649953	1	2026-09-09 11:03:17.649953	47
46	2	48	1to2	1	add	11	1	1	Traditional Approach<br/>Shankar A/c - Personal - Shankar is the Receiver - Dr<br/>Bank A/c - Personal - Bank is the Giver - Cr<br/>Accounting equation Approach<br/>Shankar A/c - Asset -  Increased - Dr<br/>Bank A/c - Asset -  Decreased - Cr	add	9	2	1	Traditional Approach<br/>Shankar A/c - Personal - Shankar is the Receiver - Dr<br/>Bank A/c - Personal - Bank is the Giver - Cr<br/>Accounting equation Approach<br/>Shankar A/c - Asset -  Increased - Dr<br/>Bank A/c - Asset -  Decreased - Cr	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 11:05:15.676473	1	2026-09-09 11:05:15.676473	48
47	2	49	1to2	1	add	14	1	1	Traditional Approach<br/>Rent A/c - Nominal - Rent Is the Expense - Dr<br/>Cash A/c - Real - Cash Going out - Cr<br/>Accounting equation Approach<br/>Rent A/c - Expense -  Increased - Dr<br/>Cash A/c - Asset -  Decreased - Cr	add	5	2	1	Traditional Approach<br/>Rent A/c - Nominal - Rent Is the Expense - Dr<br/>Cash A/c - Real - Cash Going out - Cr<br/>Accounting equation Approach<br/>Rent A/c - Expense -  Increased - Dr<br/>Cash A/c - Asset -  Decreased - Cr	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 11:09:06.464915	1	2026-09-09 11:09:06.464915	49
48	2	50	1to2	1	add	15	1		Traditional Approach<br/>Salaries A/c - Nominal - Salaries Is the Expense - Dr<br/>Cash A/c - Real - Cash Going out - Cr<br/>Accounting equation Approach<br/>Salaries A/c - Expense -  Increased - Dr<br/>Cash A/c - Asset -  Decreased - Cr	add	5	2	1	Traditional Approach<br/>Salaries A/c - Nominal - Salaries Is the Expense - Dr<br/>Cash A/c - Real - Cash Going out - Cr<br/>Accounting equation Approach<br/>Salaries A/c - Expense -  Increased - Dr<br/>Cash A/c - Asset -  Decreased - Cr	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 11:20:00.108724	1	2026-09-09 11:20:00.108724	50
49	2	51	1to2	1	add	5	\N	1	Traditional Approach<br/>Advertisement A/c - Nominal - Advertisement Is the Expense - Dr<br/>Cash A/c - Real - Cash Going out - Cr<br/>Accounting equation Approach<br/>Advertisement A/c - Expense -  Increased - Dr<br/>Cash A/c - Asset -  Decreased - Cr	add	\N	1	1	Traditional Approach<br/>Advertisement A/c - Nominal - Advertisement Is the Expense - Dr<br/>Cash A/c - Real - Cash Going out - Cr<br/>Accounting equation Approach<br/>Advertisement A/c - Expense -  Increased - Dr<br/>Cash A/c - Asset -  Decreased - Cr	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 11:22:01.491215	1	2026-09-09 11:22:01.491215	51
50	2	52	1to2	1	add	5	2	1	Traditional Approach<br/>Interest A/c - Nominal - Interest Is the Expense - Dr<br/>Cash A/c - Real - Cash Going out - Cr<br/>Accounting equation Approach<br/>Interest A/c - Expense -  Increased - Dr<br/>Cash A/c - Asset -  Decreased - Cr	add	17	1	1	Traditional Approach<br/>Interest A/c - Nominal - Interest Is the Expense - Dr<br/>Cash A/c - Real - Cash Going out - Cr<br/>Accounting equation Approach<br/>Interest A/c - Expense -  Increased - Dr<br/>Cash A/c - Asset -  Decreased - Cr	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 11:24:07.995998	1	2026-09-09 11:24:07.995998	52
51	2	53	1to2	1	add	5	1	1	Traditional Approach<br/>Cash A/c - Real - Cash Coming in - Dr<br/>Sales A/c - Nominal - Sales Is the Income - Cr<br/>Accounting equation Approach<br/>Cash A/c - Asset -  Increased - Dr<br/>Sales A/c - Revenue -  Increased - Cr	add	18	2	1	Traditional Approach<br/>Cash A/c - Real - Cash Coming in - Dr<br/>Sales A/c - Nominal - Sales Is the Income - Cr<br/>Accounting equation Approach<br/>Cash A/c - Asset -  Increased - Dr<br/>Sales A/c - Revenue -  Increased - Cr	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 11:26:05.83775	1	2026-09-09 11:26:05.83775	53
52	2	54	1to2	1	add	5	1	1	Traditional Approach<br/>Cash A/c - Real - Cash Coming in - Dr<br/>Commission received A/c - Nominal - Commission received Is the Gain - Cr<br/>Accounting equation Approach<br/>Cash A/c - Asset -  Increased - Dr<br/>Commission received A/c - Revenue -  Increased - Cr	add	19	2	1	Traditional Approach<br/>Cash A/c - Real - Cash Coming in - Dr<br/>Commission received A/c - Nominal - Commission received Is the Gain - Cr<br/>Accounting equation Approach<br/>Cash A/c - Asset -  Increased - Dr<br/>Commission received A/c - Revenue -  Increased - Cr	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 11:27:48.38666	1	2026-09-09 11:27:48.38666	54
53	2	55	1to2	1	add	20	1	1	Traditional Approach<br/>Priya A/c - Personal - Priya is the Receiver - Dr<br/>Sales A/c - Nominal - Sales Is the Income - Cr<br/>Accounting equation Approach<br/>Priya A/c - Asset -  Increased - Dr<br/>Sales A/c - Revenue -  Increased - Cr	add	18	2	1	Traditional Approach<br/>Priya A/c - Personal - Priya is the Receiver - Dr<br/>Sales A/c - Nominal - Sales Is the Income - Cr<br/>Accounting equation Approach<br/>Priya A/c - Asset -  Increased - Dr<br/>Sales A/c - Revenue -  Increased - Cr	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 11:30:41.069372	1	2026-09-09 11:30:41.069372	55
54	2	56	1to2	1	add	21	1	1	Traditional Approach<br/>Discount allowed A/c - Nominal - Discount allowed Is the Expense - Dr<br/>Rahul A/c - Personal - Rahul is the Giver of cash - Cr<br/>Accounting equation Approach<br/>Discount allowed A/c - Expense -  Increased - Dr<br/>Rahul A/c - Asset -  Decreased - Cr	add	22	2	1	Traditional Approach<br/>Discount allowed A/c - Nominal - Discount allowed Is the Expense - Dr<br/>Rahul A/c - Personal - Rahul is the Giver of cash - Cr<br/>Accounting equation Approach<br/>Discount allowed A/c - Expense -  Increased - Dr<br/>Rahul A/c - Asset -  Decreased - Cr	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 11:32:30.098416	1	2026-09-09 11:32:30.098416	56
55	2	57	1to2	1	add	23	1	1	Traditional Approach<br/>Vinay A/c - Personal - Vinay is the Receiver of cash - Dr<br/>Discount received A/c - Nominal - Discount received Is the Gain - Cr<br/>Accounting equation Approach<br/>Vinay A/c - Liability -  Decreased - Dr<br/>Discount received A/c - Revenue -  Increased - Cr	add	24	2	1	Traditional Approach<br/>Vinay A/c - Personal - Vinay is the Receiver of cash - Dr<br/>Discount received A/c - Nominal - Discount received Is the Gain - Cr<br/>Accounting equation Approach<br/>Vinay A/c - Liability -  Decreased - Dr<br/>Discount received A/c - Revenue -  Increased - Cr	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 11:34:17.9674	1	2026-09-09 11:34:17.9674	57
56	2	58	1to2	1	add	25	1	1	Traditional Approach<br/>Bad debts  A/c - Nominal - Bad debts  is the Loss - Dr<br/>Sujatha A/c - Personal - Sujatha Account to be decreased - Cr<br/>Accounting equation Approach<br/>Bad debts  A/c - Expense -  Increased - Dr<br/>Sujatha A/c - Asset -  Decreased - Cr	add	26	2	1	Traditional Approach<br/>Bad debts  A/c - Nominal - Bad debts  is the Loss - Dr<br/>Sujatha A/c - Personal - Sujatha Account to be decreased - Cr<br/>Accounting equation Approach<br/>Bad debts  A/c - Expense -  Increased - Dr<br/>Sujatha A/c - Asset -  Decreased - Cr	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 11:36:33.202508	1	2026-09-09 11:36:33.202508	58
57	2	59	1to2	1	add	27	1	1	Traditional Approach<br/>Purchases A/c - Nominal - Purchases Is the Expense - Dr<br/>Anivish A/c - Personal - Anivish is the Giver - Cr<br/>Accounting equation Approach<br/>Purchases A/c - Asset -  Increased - Dr<br/>Anivish A/c - Liability -  Increased - Cr	add	28	2	1	Traditional Approach<br/>Purchases A/c - Nominal - Purchases Is the Expense - Dr<br/>Anivish A/c - Personal - Anivish is the Giver - Cr<br/>Accounting equation Approach<br/>Purchases A/c - Asset -  Increased - Dr<br/>Anivish A/c - Liability -  Increased - Cr	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 11:38:34.361998	1	2026-09-09 11:38:34.361998	59
58	2	60	1to2	1	add	6	1	1	Traditional Approach<br/>Machinery A/c - Real - Machinery Coming in - Dr<br/>Raju A/c - Personal - Raju is the Giver - Cr<br/>Accounting equation Approach<br/>Machinery A/c - Asset -  Increased - Dr<br/>Raju A/c - Liability -  Increased - Cr	add	29	2	1	Traditional Approach<br/>Machinery A/c - Real - Machinery Coming in - Dr<br/>Raju A/c - Personal - Raju is the Giver - Cr<br/>Accounting equation Approach<br/>Machinery A/c - Asset -  Increased - Dr<br/>Raju A/c - Liability -  Increased - Cr	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 11:40:01.252893	1	2026-09-09 11:40:01.252893	60
59	2	61	1to2	1	add	30	2	1	Traditional Approach<br/>Furniture A/c - Real - Furniture Coming in - Dr<br/>Rithika A/c - Personal - Rithika is the Giver - Cr<br/>Accounting equation Approach<br/>Furniture A/c - Asset -  Increased - Dr<br/>Rithika A/c - Liability -  Increased - Cr	add	4	1	1	Traditional Approach<br/>Furniture A/c - Real - Furniture Coming in - Dr<br/>Rithika A/c - Personal - Rithika is the Giver - Cr<br/>Accounting equation Approach<br/>Furniture A/c - Asset -  Increased - Dr<br/>Rithika A/c - Liability -  Increased - Cr	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 11:41:25.415897	1	2026-09-09 11:41:25.415897	61
60	2	62	1to2	1	add	31	2	1	Traditional Approach<br/>Building A/c - Real - Building Coming in - Dr<br/>Sohail A/c - Personal - Sohail is the Giver - Cr<br/>Accounting equation Approach<br/>Building A/c - Asset -  Increased - Dr<br/>Sohail A/c - Liability -  Increased - Cr	add	8	1	1	Traditional Approach<br/>Building A/c - Real - Building Coming in - Dr<br/>Sohail A/c - Personal - Sohail is the Giver - Cr<br/>Accounting equation Approach<br/>Building A/c - Asset -  Increased - Dr<br/>Sohail A/c - Liability -  Increased - Cr	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 11:42:37.621649	1	2026-09-09 11:42:37.621649	62
61	2	63	1to2	1	add	29	1	1	Traditional Approach<br/>Raju A/c - Personal - Raju is the Receiver - Dr<br/>Cash A/c - Real - Cash Going out - Cr<br/>Accounting equation Approach<br/>Raju A/c - Liability -  Decreased - Dr<br/>Cash A/c - Asset -  Decreased - Cr	add	5	2	1	Traditional Approach<br/>Raju A/c - Personal - Raju is the Receiver - Dr<br/>Cash A/c - Real - Cash Going out - Cr<br/>Accounting equation Approach<br/>Raju A/c - Liability -  Decreased - Dr<br/>Cash A/c - Asset -  Decreased - Cr	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 11:44:22.108604	1	2026-09-09 11:44:22.108604	63
62	2	64	1to2	1	add	5	2	1	Traditional Approach<br/>Rithika A/c - Personal - Rithika is the Receiver - Dr<br/>Cash A/c - Real - Cash Going out - Cr<br/>Accounting equation Approach<br/>Rithika A/c - Liability -  Decreased - Dr<br/>Cash A/c - Asset -  Decreased - Cr	add	30	1	1	Traditional Approach<br/>Rithika A/c - Personal - Rithika is the Receiver - Dr<br/>Cash A/c - Real - Cash Going out - Cr<br/>Accounting equation Approach<br/>Rithika A/c - Liability -  Decreased - Dr<br/>Cash A/c - Asset -  Decreased - Cr	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 11:51:26.967529	1	2026-09-09 11:51:26.967529	64
63	3	65	1to2	1	add	32	1	1	Due to Purchase returns book undercast Purchase returns A/c & Trial balance was short on Credit side.<br/> Suspense A/c opened with Credit balance.<br/> In Rectification entry Debit the Suspense A/c and Credit the Purchase returns A/c.	add	34	2	1	Due to Purchase returns book undercast Purchase returns A/c & Trial balance was short on Credit side.<br/> Suspense A/c opened with Credit balance.<br/> In Rectification entry Debit the Suspense A/c and Credit the Purchase returns A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 18:26:50.166278	1	2026-09-09 18:26:50.166278	65
64	3	66	1to2	1	add	32	1	1	Due to Sales book added short Sales A/c & Trial balance was short on Credit side.<br/> Suspense A/c opened with Credit balance.<br/> In Rectification entry Debit the Suspense A/c and Credit the Sales A/c.	add	18	2	1	Due to Sales book added short Sales A/c & Trial balance was short on Credit side.<br/> Suspense A/c opened with Credit balance.<br/> In Rectification entry Debit the Suspense A/c and Credit the Sales A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 18:28:36.039562	1	2026-09-09 18:28:36.039562	66
65	3	67	1to2	1	add	66	\N	1	Due to Sales returns book undercast Sales returns A/c & Trial balance was short on Debit side.<br/> Suspense A/c opened with Debit balance.<br/> In Rectification entry Credit the Suspense A/c and Debit the Sales returns A/c.	add	32	2	1	Due to Sales returns book undercast Sales returns A/c & Trial balance was short on Debit side.<br/> Suspense A/c opened with Debit balance.<br/> In Rectification entry Credit the Suspense A/c and Debit the Sales returns A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 18:32:48.193674	1	2026-09-09 18:32:48.193674	67
66	3	68	1to2	1	add	5	1	1	Due to Cash book undercast Cash A/c & Trial balance was short on Debit side.<br/> Suspense A/c opened with Debit balance.<br/> In Rectification entry Credit the Suspense A/c and Debit the Cash A/c.	add	32	2	1	Due to Cash book undercast Cash A/c & Trial balance was short on Debit side.<br/> Suspense A/c opened with Debit balance.<br/> In Rectification entry Credit the Suspense A/c and Debit the Cash A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 18:35:30.282746	1	2026-09-09 18:35:30.282746	68
67	3	69	1to2	1	add	27	1	1	Due to Purchase book undercast Purchases A/c & Trial balance was short on Debit side.<br/> Suspense A/c opened with Debit balance.<br/> In Rectification entry Credit the Suspense A/c and Debit the Purchases A/c.	add	32	2	1	Due to Purchase book undercast Purchases A/c & Trial balance was short on Debit side.<br/> Suspense A/c opened with Debit balance.<br/> In Rectification entry Credit the Suspense A/c and Debit the Purchases A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 18:37:27.308114	1	2026-09-09 18:37:27.308114	69
68	3	70	1to2	1	add	32	1	1	Due to Purchase book overcast, Purchases A/c & Trial balance was more on Debit side.<br/> Suspense A/c opened with Credit balance.<br/> In Rectification entry Debit the Suspense A/c and Credit the Purchases A/c.	add	27	2	1	Due to Purchase book overcast, Purchases A/c & Trial balance was more on Debit side.<br/> Suspense A/c opened with Credit balance.<br/> In Rectification entry Debit the Suspense A/c and Credit the Purchases A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 18:38:55.493273	1	2026-09-09 18:38:55.493273	70
69	3	71	1to2	1	add	\N	1	1	Due to Purchase returns book overcast, Purchase returns A/c & Trial balance was more on Credit side.<br/> Suspense A/c opened with Debit balance.<br/> In Rectification entry Credit the Suspense A/c and Debit the Purchase returns A/c.	add	32	2	1	Due to Purchase returns book overcast, Purchase returns A/c & Trial balance was more on Credit side.<br/> Suspense A/c opened with Debit balance.<br/> In Rectification entry Credit the Suspense A/c and Debit the Purchase returns A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 18:40:27.708073	1	2026-09-09 18:40:27.708073	71
70	3	72	1to2	1	add	18	1	1	Due to Sales book overcast, Sales A/c & Trial balance was more on Credit side.<br/> Suspense A/c opened with Debit balance.<br/> In Rectification entry Credit the Suspense A/c and Debit the Sales A/c.	add	32	2	1	Due to Sales book overcast, Sales A/c & Trial balance was more on Credit side.<br/> Suspense A/c opened with Debit balance.<br/> In Rectification entry Credit the Suspense A/c and Debit the Sales A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 18:42:55.563808	1	2026-09-09 18:42:55.563808	72
71	3	73	1to2	1	add	32	\N	1	Due to Sales returns book overcast, Sales returns A/c & Trial balance was more on Debit side.<br/> Suspense A/c opened with Credit balance.<br/> In Rectification entry Debit the Suspense A/c and Credit the Sales returns A/c.	add	66	2	1	Due to Sales returns book overcast, Sales returns A/c & Trial balance was more on Debit side.<br/> Suspense A/c opened with Credit balance.<br/> In Rectification entry Debit the Suspense A/c and Credit the Sales returns A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 18:44:31.537343	1	2026-09-09 18:44:31.537343	73
72	3	74	1to2	1	add	32	\N	1	Due to Cash book overcast,  Cash A/c & Trial balance was more on Debit side.<br/> Suspense A/c opened with Credit balance.<br/> In Rectification entry Debit the Suspense A/c and Credit the Cash A/c.	add	5	2	1	Due to Cash book overcast,  Cash A/c & Trial balance was more on Debit side.<br/> Suspense A/c opened with Credit balance.<br/> In Rectification entry Debit the Suspense A/c and Credit the Cash A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 18:46:18.921958	1	2026-09-09 18:46:18.921958	74
73	3	75	1to2	1	add	32	1	1	Due to Less amount recorded in Lalitha A/c, Lalitha A/c & Trial balance was short on Credit side.<br/>Suspense A/c opened with Credit balance.<br/> In Rectification entry Debit the Suspense A/c and Credit the Lalitha A/c.	add	39	2	1	Due to Less amount recorded in Lalitha A/c, Lalitha A/c & Trial balance was short on Credit side.<br/>Suspense A/c opened with Credit balance.<br/> In Rectification entry Debit the Suspense A/c and Credit the Lalitha A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 18:48:26.660034	1	2026-09-09 18:48:26.660034	75
74	3	76	1to2	1	add	40	1	1	Due to more amount recorded in Amala A/c, Amala A/c & Trial balance was more on Credit side.<br/>Suspense A/c opened with Debit balance.<br/> In Rectification entry Credit the Suspense A/c and Debit the Amala A/c.	add	32	2	1	Due to more amount recorded in Amala A/c, Amala A/c & Trial balance was more on Credit side.<br/>Suspense A/c opened with Debit balance.<br/> In Rectification entry Credit the Suspense A/c and Debit the Amala A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 18:50:02.07712	1	2026-09-09 18:50:02.07712	76
75	3	77	1to2	1	add	41	1	1	Due to Less amount recorded in Rajesh A/c, Rajesh A/c & Trial balance was short on Debit side.<br/>Suspense A/c opened with Debit balance.<br/> In Rectification entry Credit the Suspense A/c and Debit the Rajesh A/c.	add	32	2	1	Due to Less amount recorded in Rajesh A/c, Rajesh A/c & Trial balance was short on Debit side.<br/>Suspense A/c opened with Debit balance.<br/> In Rectification entry Credit the Suspense A/c and Debit the Rajesh A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 18:51:26.811355	1	2026-09-09 18:51:26.811355	77
76	3	78	1to2	1	add	32	1	1	Due to more amount recorded in general expenses A/c, General Expenses A/c & Trial balance was more on Debit side.<br/>Suspense A/c opened with Credit balance.<br/> In Rectification entry Debit the Suspense A/c and Credit the General Expenses A/c.	add	42	2	1	Due to more amount recorded in general expenses A/c, General Expenses A/c & Trial balance was more on Debit side.<br/>Suspense A/c opened with Credit balance.<br/> In Rectification entry Debit the Suspense A/c and Credit the General Expenses A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 18:52:51.15725	1	2026-09-09 18:52:51.15725	78
77	3	79	1to2	1	add	32	1	1	Due to Less amount recorded in Sales A/c, Sales A/c & Trial balance was short on Credit side.<br/>Suspense A/c opened with Credit balance.<br/> In Rectification entry Debit the Suspense A/c and Credit the Sales A/c.	add	18	\N	1	Due to Less amount recorded in Sales A/c, Sales A/c & Trial balance was short on Credit side.<br/>Suspense A/c opened with Credit balance.<br/> In Rectification entry Debit the Suspense A/c and Credit the Sales A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 18:55:02.737014	1	2026-09-09 18:55:02.737014	79
78	3	80	1to1	1	add	43	1	1	Due to Goods sold to Ashok is not recorded  in his account, Ashok A/c & Trial balance was short on Debit side.<br/>Suspense A/c opened with Debit balance.<br/> In Rectification entry Credit the Suspense A/c and Debit the Ashok A/c.	add	32	2	1	Due to Goods sold to Ashok is not recorded  in his account, Ashok A/c & Trial balance was short on Debit side.<br/>Suspense A/c opened with Debit balance.<br/> In Rectification entry Credit the Suspense A/c and Debit the Ashok A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 18:56:24.410103	1	2026-09-09 18:56:24.410103	80
79	3	81	1to2	1	add	32	1	1	Due to Goods returned by Ramesh is recorded  in his account, Ramesh A/c & Trial balance was short on Credit side.<br/>Suspense A/c opened with Credit balance.<br/> In Rectification entry Debit the Suspense A/c and Credit the Ramesh A/c.	add	44	2	1	Due to Goods returned by Ramesh is recorded  in his account, Ramesh A/c & Trial balance was short on Credit side.<br/>Suspense A/c opened with Credit balance.<br/> In Rectification entry Debit the Suspense A/c and Credit the Ramesh A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 18:58:39.434363	1	2026-09-09 18:58:39.434363	81
80	3	82	1to2	1	add	32	1	1	Due to Cash discount allowed to Amar is not recorded  in his account, Amar A/c & Trial balance was short on Credit side.<br/>Suspense A/c opened with Credit balance.<br/> In Rectification entry Debit the Suspense A/c and Credit the Amar A/c.	add	45	2	1	Due to Cash discount allowed to Amar is not recorded  in his account, Amar A/c & Trial balance was short on Credit side.<br/>Suspense A/c opened with Credit balance.<br/> In Rectification entry Debit the Suspense A/c and Credit the Amar A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 19:00:22.185344	1	2026-09-09 19:00:22.185344	82
81	3	83	1to2	1	add	24	2	1	Due to Discount received is not recorded, Discount received A/c & Trial balance was short on Credit side.<br/>Suspense A/c opened with Credit balance.<br/> In Rectification entry Debit the Suspense A/c and Credit the Discount received A/c.	add	32	1	1	Due to Discount received is not recorded, Discount received A/c & Trial balance was short on Credit side.<br/>Suspense A/c opened with Credit balance.<br/> In Rectification entry Debit the Suspense A/c and Credit the Discount received A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 19:01:50.746159	1	2026-09-09 19:01:50.746159	83
82	3	84	1to2	1	add	32	1	1	Due to  this error, Sharath  A/c & Trial balance were more on Debit side with Double amount of the error .<br/>Suspense A/c opened with Credit balance.<br/> In Rectification entry Debit the Suspense A/c and Credit the Sharath  A/c.	add	46	\N	1	Due to  this error, Sharath  A/c & Trial balance were more on Debit side with Double amount of the error .<br/>Suspense A/c opened with Credit balance.<br/> In Rectification entry Debit the Suspense A/c and Credit the Sharath  A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 19:04:44.565953	1	2026-09-09 19:04:44.565953	84
83	3	85	1to2	1	add	32	1	1	Due to  this error, Vinay A/c & Trial balance were more on Debit side with Double amount of the error .<br/>Suspense A/c opened with Credit balance.<br/> In Rectification entry Debit the Suspense A/c and Credit the Vinay A/c.	add	23	2	1	Due to  this error, Vinay A/c & Trial balance were more on Debit side with Double amount of the error .<br/>Suspense A/c opened with Credit balance.<br/> In Rectification entry Debit the Suspense A/c and Credit the Vinay A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 19:08:53.76885	1	2026-09-09 19:08:53.76885	85
84	3	86	1to2	1	add	48	2	1	Due to  this error, Mukesh A/c & Trial balance were more on Debit side with Double amount of the error .<br/>Suspense A/c opened with Credit balance.<br/> In Rectification entry Debit the Suspense A/c and Credit the Mukesh A/c.	add	32	1	1	Due to  this error, Mukesh A/c & Trial balance were more on Debit side with Double amount of the error .<br/>Suspense A/c opened with Credit balance.<br/> In Rectification entry Debit the Suspense A/c and Credit the Mukesh A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 19:10:33.885657	1	2026-09-09 19:10:33.885657	86
85	3	87	1to2	1	add	32	1	1	Due to  this error, Kiran A/c & Trial balance were more on Debit side with Double amount of the error .<br/>Suspense A/c opened with Credit balance.<br/> In Rectification entry Debit the Suspense A/c and Credit the Kiran A/c.	add	47	2	1	Due to  this error, Kiran A/c & Trial balance were more on Debit side with Double amount of the error .<br/>Suspense A/c opened with Credit balance.<br/> In Rectification entry Debit the Suspense A/c and Credit the Kiran A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 19:12:20.626895	1	2026-09-09 19:12:20.626895	87
86	3	88	1to2	1	add	49	1	1	Due to this error, Rafi A/c & Trial balance were more on Credit side with Double amount of the error.<br/>Suspense A/c opened with Debit balance.<br/> In Rectification entry Credit the Suspense A/c and Debit the Rafi A/c.	add	32	2	1	Due to this error, Rafi A/c & Trial balance were more on Credit side with Double amount of the error.<br/>Suspense A/c opened with Debit balance.<br/> In Rectification entry Credit the Suspense A/c and Debit the Rafi A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 19:52:43.12607	1	2026-09-09 19:52:43.12607	88
87	3	89	1to2	1	add	32	2	1	Due to this error, Ramesh A/c & Trial balance were more on Credit side with Double amount of the error.<br/>Suspense A/c opened with Debit balance.<br/> In Rectification entry Credit the Suspense A/c and Debit the Ramesh A/c.	add	44	1	1	Due to this error, Ramesh A/c & Trial balance were more on Credit side with Double amount of the error.<br/>Suspense A/c opened with Debit balance.<br/> In Rectification entry Credit the Suspense A/c and Debit the Ramesh A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 19:54:01.977079	1	2026-09-09 19:54:01.977079	89
88	3	90	1to2	1	add	27	1	1	Due to  this error, Purchases A/c & Trial balance were more on Credit side with Double amount of the error.<br/>Suspense A/c opened with Debit balance.<br/> In Rectification entry Credit the Suspense A/c and Debit the Purchases A/c.	add	32	2	1	Due to  this error, Purchases A/c & Trial balance were more on Credit side with Double amount of the error.<br/>Suspense A/c opened with Debit balance.<br/> In Rectification entry Credit the Suspense A/c and Debit the Purchases A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 19:55:28.238026	1	2026-09-09 19:55:28.238026	90
89	3	91	1to2	1	add	32	2	1	Due to this error, Cash A/c & Trial balance were more on Credit side with Double amount of the error.<br/>Suspense A/c opened with Debit balance.<br/> In Rectification entry Credit the Suspense A/c and Debit the Cash A/c.	add	5	1	1	Due to this error, Cash A/c & Trial balance were more on Credit side with Double amount of the error.<br/>Suspense A/c opened with Debit balance.<br/> In Rectification entry Credit the Suspense A/c and Debit the Cash A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 19:56:43.651771	1	2026-09-09 19:56:43.651771	91
90	3	92	1to2	1	add	51	1	1	Due to  this error, Cash A/c was wrongly Credited.<br/>Required entry not recorded in Bank A/c.<br/> In Rectification entry Debit the Cash A/c and Credit the Bank A/c.	add	52	2	1	Due to  this error, Cash A/c was wrongly Credited.<br/>Required entry not recorded in Bank A/c.<br/> In Rectification entry Debit the Cash A/c and Credit the Bank A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 20:00:28.172121	1	2026-09-09 20:00:28.172121	92
91	3	93	1to2	1	add	50	1	1	Due to  this error, Rent Received A/c was wrongly Credited.<br/>Required entry not recorded in Discount Received A/c.<br/> In Rectification entry Debit the Rent Received A/c and Credit the Discount Received A/c.	add	24	2	1	Due to  this error, Rent Received A/c was wrongly Credited.<br/>Required entry not recorded in Discount Received A/c.<br/> In Rectification entry Debit the Rent Received A/c and Credit the Discount Received A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 20:02:01.639069	1	2026-09-09 20:02:01.639069	93
92	3	94	1to2	1	add	17	1	1	Due to  this error, Rent Received A/c was wrongly Credited.<br/>Required entry not recorded in Discount Received A/c.<br/> In Rectification entry Debit the Rent Received A/c and Credit the Discount Received A/c.	add	19	2	1	Due to  this error, Rent Received A/c was wrongly Credited.<br/>Required entry not recorded in Discount Received A/c.<br/> In Rectification entry Debit the Rent Received A/c and Credit the Discount Received A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 20:03:20.292617	1	2026-09-09 20:03:20.292617	94
93	3	95	1to2	1	add	5	1	1	Due to  this error, Cash A/c was wrongly Credited.<br/>Required entry not recorded in Bank A/c.<br/> In Rectification entry Debit the Cash A/c and Credit the Bank A/c.	add	9	2	1	Due to  this error, Cash A/c was wrongly Credited.<br/>Required entry not recorded in Bank A/c.<br/> In Rectification entry Debit the Cash A/c and Credit the Bank A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 20:04:47.956315	1	2026-09-09 20:04:47.956315	95
94	3	96	1to2	1	add	56	1	1	Due to  this error, Prabhas A/c was wrongly Debited.<br/>Required entry not recorded in Pradeep A/c.<br/> In Rectification entry Credit the Prabhas A/c and Debit the Pradeep A/c.	add	55	2	1	Due to  this error, Prabhas A/c was wrongly Debited.<br/>Required entry not recorded in Pradeep A/c.<br/> In Rectification entry Credit the Prabhas A/c and Debit the Pradeep A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 20:09:02.249902	1	2026-09-09 20:09:02.249902	96
95	3	97	1to2	1	add	58	2	1	Due to  this error, Shyam A/c was wrongly Debited.<br/>Required entry not recorded in Ram A/c.<br/> In Rectification entry Credit the Shyam A/c and Debit the Ram A/c.	add	57	1	1	Due to  this error, Shyam A/c was wrongly Debited.<br/>Required entry not recorded in Ram A/c.<br/> In Rectification entry Credit the Shyam A/c and Debit the Ram A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 20:10:14.581342	1	2026-09-09 20:10:14.581342	97
96	3	98	1to2	1	add	54	1	1	Due to  this error, Srinu A/c was wrongly Debited.<br/>Required entry not recorded in Mohan A/c.<br/> In Rectification entry Credit the Srinu A/c and Debit the Mohan A/c.	add	59	2	1	Due to  this error, Srinu A/c was wrongly Debited.<br/>Required entry not recorded in Mohan A/c.<br/> In Rectification entry Credit the Srinu A/c and Debit the Mohan A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 20:11:21.60976	1	2026-09-09 20:11:21.60976	98
97	3	99	1to2	1	add	17	1	1	Due to  this error, Commission A/c was wrongly Debited.<br/>Required entry not recorded in Interest A/c.<br/> In Rectification entry Credit the Commission A/c and Debit the Interest A/c.	add	53	2	1	Due to  this error, Commission A/c was wrongly Debited.<br/>Required entry not recorded in Interest A/c.<br/> In Rectification entry Credit the Commission A/c and Debit the Interest A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 20:13:29.201992	1	2026-09-09 20:13:29.201992	99
98	3	100	1to2	1	add	61	1	1	Due to  this error, Building A/c was wrongly Debited.<br/>Required entry not recorded in Repairs A/c.<br/> In Rectification entry Credit the Building A/c and Debit the Repairs A/c.	add	8	2	1	Due to  this error, Building A/c was wrongly Debited.<br/>Required entry not recorded in Repairs A/c.<br/> In Rectification entry Credit the Building A/c and Debit the Repairs A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 20:19:25.489034	1	2026-09-09 20:19:25.489034	100
99	3	101	1to2	1	add	60	2	1	Due to  this error, Mr.Murali A/c was wrongly Debited.<br/>Required entry not recorded in Rent A/c.<br/> In Rectification entry Credit the Mr.Murali A/c and Debit the Rent A/c.	add	14	1	1	Due to  this error, Mr.Murali A/c was wrongly Debited.<br/>Required entry not recorded in Rent A/c.<br/> In Rectification entry Credit the Mr.Murali A/c and Debit the Rent A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 20:57:04.366353	1	2026-09-09 20:57:04.366353	101
100	3	102	1to2	1	add	63	1	1	Due to  this error, Office Expenses A/c was wrongly Debited.<br/>Required entry not recorded in Typewrier A/c.<br/> In Rectification entry Credit the Office Expenses A/c and Debit the Typewriter A/c.	add	68	2	1	Due to  this error, Office Expenses A/c was wrongly Debited.<br/>Required entry not recorded in Typewrier A/c.<br/> In Rectification entry Credit the Office Expenses A/c and Debit the Typewriter A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 21:12:07.04732	1	2026-09-09 21:12:07.04732	102
101	3	103	1to2	1	add	13	1	1	Due to  this error, Trade expenses A/c was wrongly Debited.<br/>Required entry not recorded in Drawings A/c.<br/> In Rectification entry Credit the Trade expenses A/c and Debit the Drawings A/c.	add	62	2	1	Due to  this error, Trade expenses A/c was wrongly Debited.<br/>Required entry not recorded in Drawings A/c.<br/> In Rectification entry Credit the Trade expenses A/c and Debit the Drawings A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 21:15:54.680162	1	2026-09-09 21:15:54.680162	103
102	3	104	1to2	1	add	4	1	1	Due to  this error, Purchases A/c was wrongly Debited.<br/>Required entry not recorded in Furniture A/c.<br/> In Rectification entry Credit the Purchases A/c and Debit the Furniture A/c.	add	27	2	1	Due to  this error, Purchases A/c was wrongly Debited.<br/>Required entry not recorded in Furniture A/c.<br/> In Rectification entry Credit the Purchases A/c and Debit the Furniture A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 21:24:16.838573	1	2026-09-09 21:24:16.838573	104
103	3	105	1to2	1	add	67	1		Due to complete omission  Required entry not recorded in Sudha A/c & Sales A/c.<br/> In Rectification entry Debit the Sudha A/c and Credit the Sales A/c.	add	18	2	1	Due to complete omission  Required entry not recorded in Sudha A/c & Sales A/c.<br/> In Rectification entry Debit the Sudha A/c and Credit the Sales A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 21:30:36.347072	1	2026-09-09 21:30:36.347072	105
104	3	106	1to2	1	add	44	2	1	Due to complete omission  Required entry not recorded in Sales returns A/c & Ramesh A/c.<br/> In Rectification entry Debit the Sales returns A/c and Credit the Ramesh A/c.	add	66	1	1	Due to complete omission  Required entry not recorded in Sales returns A/c & Ramesh A/c.<br/> In Rectification entry Debit the Sales returns A/c and Credit the Ramesh A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 21:32:24.617587	1	2026-09-09 21:32:24.617587	106
105	3	107	1to2	1	add	27	1	1	Due to complete omission  Required entry not recorded in Purchases A/c & Mohan A/c.<br/> In Rectification entry Debit the Purchases A/c and Credit the Mohan A/c.	add	54	2	1	Due to complete omission  Required entry not recorded in Purchases A/c & Mohan A/c.<br/> In Rectification entry Debit the Purchases A/c and Credit the Mohan A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 21:34:22.800154	1	2026-09-09 21:34:22.800154	107
106	3	108	1to2	1	add	34	\N	1	Due to complete omission  Required entry not recorded in Pavankumar A/c & Purchase returns A/c.<br/> In Rectification entry Debit the Pavankumar A/c and Credit the Purchase returns A/c.	add	64	1	1	Due to complete omission  Required entry not recorded in Pavankumar A/c & Purchase returns A/c.<br/> In Rectification entry Debit the Pavankumar A/c and Credit the Purchase returns A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 21:35:54.874345	1	2026-09-09 21:35:54.874345	108
107	3	109	1to2	1	add	65	1	1	Due to complete omission  Required entry not recorded in Salary A/c & cash A/c.<br/> In Rectification entry Debit the Salary A/c and Credit the cash A/c.	add	5	2	1	Due to complete omission  Required entry not recorded in Salary A/c & cash A/c.<br/> In Rectification entry Debit the Salary A/c and Credit the cash A/c.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	t	2026-09-09 21:37:29.661667	1	2026-09-09 21:37:29.661667	109
\.


--
-- TOC entry 5459 (class 0 OID 30286)
-- Dependencies: 259
-- Data for Name: section; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.section (section_id, course_id, section_name, description, active_row, created_at, updated_at, college_id, branch_id) FROM stdin;
\.


--
-- TOC entry 5461 (class 0 OID 30301)
-- Dependencies: 261
-- Data for Name: subject; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.subject (subject_id, subject_name, course_id, active_row, row_status, created_at, updated_at) FROM stdin;
1	Maths	1	t	1	2026-09-08 16:57:14.532997	2026-09-08 16:57:14.532997
2	Economics	1	t	1	2026-09-08 16:57:33.713915	2026-09-08 16:57:33.713915
3	Commerce	1	t	1	2026-09-08 16:58:11.444654	2026-09-08 16:58:11.444654
\.


--
-- TOC entry 5463 (class 0 OID 30312)
-- Dependencies: 263
-- Data for Name: subscription_plans; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.subscription_plans (plan_id, name, description, free_trial, active, duration_days, practice_question_limit, mock_test_enabled, mock_test_limit, exam_enabled, exam_attempt_limit, created_at, updated_at) FROM stdin;
1	Free Trial	7-day introduction to one course	t	t	7	25	f	0	f	0	2026-09-08 12:21:50.129077	2026-09-08 12:21:50.129077
2	Basic	Entry-level course access	f	t	30	100	f	0	f	0	2026-09-08 12:21:50.129077	2026-09-08 12:21:50.129077
3	Standard	Full practice, mock tests and exams	f	t	30	500	t	10	t	3	2026-09-08 12:21:50.129077	2026-09-08 12:21:50.129077
4	Premium	Unlimited course learning access	f	t	365	-1	t	-1	t	-1	2026-09-08 12:21:50.129077	2026-09-08 12:21:50.129077
\.


--
-- TOC entry 5465 (class 0 OID 30332)
-- Dependencies: 265
-- Data for Name: table_attributes; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.table_attributes (attribute_id, header_id, name, row_status, active_row, created_at, updated_at, amount1, amount2, row_disable) FROM stdin;
1	1	Opening Stock	RULE	t	2026-09-08 17:24:16.899466	2026-09-08 17:34:10.828409	\N	\N	f
2	1	Purchases	RULE	t	2026-09-08 17:24:40.826508	2026-09-08 17:40:41.167248	\N	\N	f
3	1	Wages	RULE	t	2026-09-08 17:25:00.85529	2026-09-08 17:43:14.306127	\N	\N	f
4	1	Sales Returns	RULE	t	2026-09-08 17:25:13.186353	2026-09-08 17:48:09.493793	\N	\N	f
5	1	Carriage	RULE	t	2026-09-08 17:25:31.818948	2026-09-08 17:49:21.429263	\N	\N	f
6	1	Coal & Gas	RULE	t	2026-09-08 17:30:48.321796	2026-09-08 17:50:39.264818	\N	\N	f
7	1	Factory Rent	RULE	t	2026-09-08 17:30:59.333679	2026-09-08 17:52:53.251052	\N	\N	f
8	2	Sales	RULE	t	2026-09-08 17:31:10.431485	2026-09-08 17:55:34.462056	\N	\N	f
9	2	Purchase Returns	RULE	t	2026-09-08 17:31:23.959575	2026-09-08 18:04:47.517052	\N	\N	f
13	2	Interest Received	DRAFT	t	2026-09-08 18:26:22.409421	2026-09-08 18:26:22.409421	\N	\N	f
20	2	Outstanding Wages	DRAFT	t	2026-09-08 18:28:45.65085	2026-09-08 18:28:45.65085	\N	\N	f
10	1	Salaries	RULE	t	2026-09-08 18:25:42.562433	2026-09-08 18:39:33.667113	\N	\N	f
11	1	Rent	RULE	t	2026-09-08 18:25:51.165607	2026-09-08 18:41:33.852841	\N	\N	f
12	1	Import Duty	RULE	t	2026-09-08 18:26:01.859468	2026-09-08 18:42:53.47146	\N	\N	f
14	2	Discount Received	RULE	t	2026-09-08 18:26:42.157893	2026-09-08 18:54:29.552212	\N	\N	f
15	1	Plant & Machinery	RULE	t	2026-09-08 18:27:21.017682	2026-09-08 18:58:26.539832	\N	\N	f
38	1	Coal & Water	RULE	t	2026-09-08 19:00:10.303703	2026-09-08 19:01:01.610238	\N	\N	f
16	1	Interest On Capital	RULE	t	2026-09-08 18:27:35.064225	2026-09-08 19:02:42.995046	\N	\N	f
17	2	Capital	RULE	t	2026-09-08 18:27:52.414807	2026-09-08 19:10:44.800269	\N	\N	f
18	2	Creditors	RULE	t	2026-09-08 18:28:02.321515	2026-09-08 19:12:11.453335	\N	\N	f
19	2	Return Outwards	RULE	t	2026-09-08 18:28:18.388823	2026-09-08 19:13:41.94433	\N	\N	f
21	2	Dividend Received	RULE	t	2026-09-08 18:29:03.003911	2026-09-08 19:16:26.812584	\N	\N	f
22	1	Fuel & Power	RULE	t	2026-09-08 18:29:39.345831	2026-09-08 19:19:27.163112	\N	\N	f
23	1	Gas & Water	RULE	t	2026-09-08 18:29:51.083084	2026-09-08 19:21:13.342516	\N	\N	f
24	1	Office Expenses	RULE	t	2026-09-08 18:30:06.949489	2026-09-08 19:22:28.689016	\N	\N	f
25	1	Postage	RULE	t	2026-09-08 18:30:14.705522	2026-09-08 19:23:34.754355	\N	\N	f
26	1	Investments	RULE	t	2026-09-08 18:30:25.662922	2026-09-08 19:24:54.705801	\N	\N	f
27	1	Land & Building	RULE	t	2026-09-08 18:30:44.537061	2026-09-08 19:25:54.335846	\N	\N	f
28	1	Good Will	RULE	t	2026-09-08 18:30:54.496294	2026-09-08 19:27:34.787542	\N	\N	f
29	2	Interest On Drawings	RULE	t	2026-09-08 18:31:17.00234	2026-09-08 19:28:53.861618	\N	\N	f
30	2	Miscellaneous Income	RULE	t	2026-09-08 18:31:37.237508	2026-09-08 19:29:54.818036	\N	\N	f
31	1	Lighting	RULE	t	2026-09-08 18:32:05.62601	2026-09-08 19:33:14.606145	\N	\N	f
32	1	Oil & Fuel	RULE	t	2026-09-08 18:32:17.301962	2026-09-08 19:34:25.949731	\N	\N	f
33	1	Printing & Stationery	RULE	t	2026-09-08 18:32:41.163629	2026-09-08 19:35:28.424993	\N	\N	f
34	1	Printing Charge	RULE	t	2026-09-08 18:32:53.111793	2026-09-08 19:38:35.70275	\N	\N	f
35	1	Rent Paid In Advance	RULE	t	2026-09-08 18:33:50.23601	2026-09-08 19:40:02.66482	\N	\N	f
36	2	Dividend	RULE	t	2026-09-08 18:34:56.314487	2026-09-08 19:42:02.707306	\N	\N	f
37	2	Outstanding Rent	RULE	t	2026-09-08 18:35:06.549617	2026-09-08 19:43:13.448094	\N	\N	f
39	3	Furniture purchased for cash	RULE	t	2026-09-09 10:12:00.927382	2026-09-09 10:36:29.101706	\N	\N	f
40	3	Machinery purchased for cash	RULE	t	2026-09-09 10:12:10.503419	2026-09-09 10:38:23.234208	\N	\N	f
41	3	Computer purchased	RULE	t	2026-09-09 10:12:22.283759	2026-09-09 10:41:03.508633	\N	\N	f
42	3	Machinery sold for cash	RULE	t	2026-09-09 10:12:39.33638	2026-09-09 10:43:13.004024	\N	\N	f
43	3	Building sold for cash	RULE	t	2026-09-09 10:12:46.897721	2026-09-09 10:44:45.535996	\N	\N	f
44	3	Mr. Ganesh commenced business by cheque	RULE	t	2026-09-09 10:13:03.210471	2026-09-09 10:46:25.091984	\N	\N	f
45	3	Cheque received from Shankar	RULE	t	2026-09-09 10:13:10.646772	2026-09-09 10:49:37.647137	\N	\N	f
46	3	Cheque given to Vaishali	RULE	t	2026-09-09 10:13:18.122386	2026-09-09 10:56:35.147938	\N	\N	f
47	3	Ganesh withdrawn from bank for personal use	RULE	t	2026-09-09 10:13:26.226921	2026-09-09 11:03:17.662736	\N	\N	f
48	3	Dishonour of cheque given by Shankar	RULE	t	2026-09-09 10:13:40.711131	2026-09-09 11:05:15.678914	\N	\N	f
49	3	Rent paid by cash	RULE	t	2026-09-09 10:13:53.632051	2026-09-09 11:09:06.464915	\N	\N	f
50	3	Salaries paid by cash	RULE	t	2026-09-09 10:14:01.717919	2026-09-09 11:20:00.11473	\N	\N	f
51	3	Advertisement expenses paid by cash	RULE	t	2026-09-09 10:14:09.456636	2026-09-09 11:22:01.491215	\N	\N	f
52	3	Interest paid	RULE	t	2026-09-09 10:14:17.348612	2026-09-09 11:24:07.996975	\N	\N	f
53	3	Goods sold for Cash	RULE	t	2026-09-09 10:14:25.275851	2026-09-09 11:26:05.838751	\N	\N	f
54	3	Commission received	RULE	t	2026-09-09 10:14:36.54472	2026-09-09 11:27:48.393658	\N	\N	f
55	3	Goods sold to Priya	RULE	t	2026-09-09 10:14:52.142664	2026-09-09 11:30:41.070371	\N	\N	f
56	3	Discount allowed to Rahul	RULE	t	2026-09-09 10:15:00.196473	2026-09-09 11:32:30.099415	\N	\N	f
57	3	Discount received from Vinay	RULE	t	2026-09-09 10:15:07.464811	2026-09-09 11:34:17.968973	\N	\N	f
58	3	Amount cannot be collected (Bad debts) from Sujatha	RULE	t	2026-09-09 10:15:15.682213	2026-09-09 11:36:33.203509	\N	\N	f
59	3	Goods purchased from Anivish	RULE	t	2026-09-09 10:15:24.369711	2026-09-09 11:38:34.362996	\N	\N	f
60	3	Machinery purchased from Raju	RULE	t	2026-09-09 10:15:45.728597	2026-09-09 11:40:01.253894	\N	\N	f
61	3	Furniture purchased from Rithika	RULE	t	2026-09-09 10:15:53.824861	2026-09-09 11:41:25.415897	\N	\N	f
62	3	Building purchased from Sohail	RULE	t	2026-09-09 10:16:01.557366	2026-09-09 11:42:37.62259	\N	\N	f
63	3	Cash paid to Raju	RULE	t	2026-09-09 10:16:11.660101	2026-09-09 11:44:22.108604	\N	\N	f
64	3	Cash paid to Rithika	RULE	t	2026-09-09 10:16:19.500737	2026-09-09 11:51:26.97381	\N	\N	f
66	3	Sales book added Rs.400 short.	RULE	t	2026-09-09 12:27:29.456082	2026-09-09 18:28:36.040571	\N	\N	f
67	3	Sales returns book under cast by Rs.500	RULE	t	2026-09-09 12:27:29.46573	2026-09-09 18:32:48.194684	\N	\N	f
68	3	Cash book undercast by Rs.200	RULE	t	2026-09-09 12:27:29.474393	2026-09-09 18:35:30.283744	\N	\N	f
69	3	Purchase book undercast Rs.260	RULE	t	2026-09-09 12:27:29.486957	2026-09-09 18:37:27.308114	\N	\N	f
70	3	Purchases book overcast by Rs.650	RULE	t	2026-09-09 12:27:29.498902	2026-09-09 18:38:55.494274	\N	\N	f
71	3	Purchases return book overcast by Rs.1450	RULE	t	2026-09-09 12:27:29.508899	2026-09-09 18:40:27.709069	\N	\N	f
72	3	Sales book was overcast by Rs.600	RULE	t	2026-09-09 12:27:29.518264	2026-09-09 18:42:55.564808	\N	\N	f
73	3	Sales returns book was overcast by Rs.600	RULE	t	2026-09-09 12:27:29.527824	2026-09-09 18:44:31.537343	\N	\N	f
110	3	Sale of goods to Sandhya & Co.Rs.2900 entered in Purchase book	DRAFT	t	2026-09-09 12:27:30.743741	2026-09-09 12:27:30.743741	\N	\N	f
111	3	A Purchase of Rs.700 from Gupta & co.,entered in Sales book	DRAFT	t	2026-09-09 12:27:30.751144	2026-09-09 12:27:30.751144	\N	\N	f
112	3	A sale of goods to Adithya Rs.2500 recorded in Purchase book	DRAFT	t	2026-09-09 12:27:30.758671	2026-09-09 12:27:30.758671	\N	\N	f
113	3	A purchase from Vaishnavi for Rs.1000 passed in Sales book	DRAFT	t	2026-09-09 12:27:30.765846	2026-09-09 12:27:30.765846	\N	\N	f
114	3	Cheque deposited into bank posted on the credit side of Cash account.	DRAFT	t	2026-09-09 12:27:30.772847	2026-09-09 12:27:30.772847	\N	\N	f
115	3	Goods returned by Shailesh Rs.1200entered in Return Outward book	DRAFT	t	2026-09-09 12:27:30.779362	2026-09-09 12:27:30.779362	\N	\N	f
65	3	Purchase returns book was undercast by Rs.500	RULE	t	2026-09-09 12:27:29.379289	2026-09-09 18:26:50.169284	\N	\N	f
74	3	Cash book overcast on debit side by Rs.850	RULE	t	2026-09-09 12:27:29.535824	2026-09-09 18:46:18.922551	\N	\N	f
75	3	Received cash from Lalitha Rs.200 has been posted to her account	RULE	t	2026-09-09 12:27:29.551739	2026-09-09 18:48:26.661037	\N	\N	f
76	3	Received of 222 from Amala has been entered in her account of Rs. 2222.	RULE	t	2026-09-09 12:27:29.560733	2026-09-09 18:50:02.078076	\N	\N	f
77	3	Sold goods to Rajesh Rs.296 in his account posted as Rs.269.	RULE	t	2026-09-09 12:27:29.575442	2026-09-09 18:51:26.811355	\N	\N	f
78	3	Rs.115 paid for general expenses but entered in account as Rs.150.	RULE	t	2026-09-09 12:27:29.609435	2026-09-09 18:52:51.15825	\N	\N	f
79	3	sold goods to Mahesh Rs.540 is entered is Sales book as Rs.450	RULE	t	2026-09-09 12:27:29.617529	2026-09-09 18:55:02.738019	\N	\N	f
80	3	Goods sold to Ashok Rs.75 were omitted to be entered in his account.	RULE	t	2026-09-09 12:27:29.625529	2026-09-09 18:56:24.418214	\N	\N	f
81	3	Goods returned from Ramesh Rs.650 were not posted to his account	RULE	t	2026-09-09 12:27:29.633177	2026-09-09 18:58:39.434363	\N	\N	f
82	3	Cash discount allowed to Amar Rs.225 entered in cash book but not posted to his personal account.	RULE	t	2026-09-09 12:27:29.639174	2026-09-09 19:00:22.186224	\N	\N	f
83	3	Discount received Rs.150 entered in cash book but not entered in discount book.	RULE	t	2026-09-09 12:27:29.661672	2026-09-09 19:01:50.747174	\N	\N	f
84	3	Received Rs.350 from Sharath was posted on Debit side of his A/c	RULE	t	2026-09-09 12:27:29.669206	2026-09-09 19:04:44.565953	\N	\N	f
85	3	Purchased goods from Vinay Rs.800 recorded correctly in purchase book but wrongly debited to his account.	RULE	t	2026-09-09 12:27:29.674311	2026-09-09 19:08:53.76985	\N	\N	f
86	3	A return of goods Rs.120 from Mukesh posted to debit of his account.	RULE	t	2026-09-09 12:27:29.680777	2026-09-09 19:10:33.886656	\N	\N	f
87	3	Rs.1050 received from Kiran were posted to the debit side of his account.	RULE	t	2026-09-09 12:27:29.68571	2026-09-09 19:12:20.627901	\N	\N	f
88	3	A payment of Rs.215 to Rafi posted to his on credit side	RULE	t	2026-09-09 12:27:29.690251	2026-09-09 19:52:43.135601	\N	\N	f
89	3	A credit sale of Rs.200 to Ramesh though properly entered in the sales book has been credited to his account.	RULE	t	2026-09-09 12:27:29.695255	2026-09-09 19:54:01.977079	\N	\N	f
90	3	Purchase of Goods Rs. 1500 posted on Credit side of Purchase account.	RULE	t	2026-09-09 12:27:29.790864	2026-09-09 19:55:28.239039	\N	\N	f
91	3	Cash received Rs.1200 posted on Credit side of Cash account	RULE	t	2026-09-09 12:27:29.797856	2026-09-09 19:56:43.653782	\N	\N	f
92	3	Rs.2000 paid by Sudheer was wrongly credited to Sandeep A/c	RULE	t	2026-09-09 12:27:29.802909	2026-09-09 20:00:28.174655	\N	\N	f
93	3	Discount Received Rs. 1500 is Recorded in Rent Received A/c	RULE	t	2026-09-09 12:27:29.820778	2026-09-09 20:02:01.640088	\N	\N	f
94	3	Commission of Rs.200 received was wrongly credited to Interest account.	RULE	t	2026-09-09 12:27:30.43277	2026-09-09 20:03:20.292617	\N	\N	f
95	3	Salaries paid By cheque but credited to Cash A/c	RULE	t	2026-09-09 12:27:30.55665	2026-09-09 20:04:47.957317	\N	\N	f
96	3	A credit sale of Rs. 1500 to Pradeep was debited in Prabhas A/c	RULE	t	2026-09-09 12:27:30.582615	2026-09-09 20:09:02.251442	\N	\N	f
97	3	Payment of Rs.500 to Ram debited to Shyam	RULE	t	2026-09-09 12:27:30.614803	2026-09-09 20:10:14.581342	\N	\N	f
98	3	Credit sales to Mohan Rs.5000 were posted to Srinu A/c	RULE	t	2026-09-09 12:27:30.626641	2026-09-09 20:11:21.60976	\N	\N	f
99	3	Rs.100 paid on account of interest was debited to commission A/c	RULE	t	2026-09-09 12:27:30.636052	2026-09-09 20:13:29.202989	\N	\N	f
100	3	Rs.2000 for the repairs of building was debited to building A/c	RULE	t	2026-09-09 12:27:30.646109	2026-09-09 20:19:25.489034	\N	\N	f
101	3	Paid rent to owner Mr. Murali Rs.5000 debited to his A/c	RULE	t	2026-09-09 12:27:30.655639	2026-09-09 20:57:04.381142	\N	\N	f
102	3	Rs.1500 paid for typewriter, debited to office expense A/c	RULE	t	2026-09-09 12:27:30.663162	2026-09-09 21:12:07.055216	\N	\N	f
103	3	Rs.850 used by proprietor has been debited to trade expense A/c	RULE	t	2026-09-09 12:27:30.675187	2026-09-09 21:15:54.68116	\N	\N	f
104	3	Rs. 500 paid for furniture purchased has been charged to Ordinary purchases account.	RULE	t	2026-09-09 12:27:30.684284	2026-09-09 21:24:16.840453	\N	\N	f
105	3	Goods sold to Sudha Rs.4000, not recorded in the Books.	RULE	t	2026-09-09 12:27:30.692801	2026-09-09 21:30:36.347577	\N	\N	f
106	3	Goods returned Rs.235 by Ramesh were not recorded in the books	RULE	t	2026-09-09 12:27:30.705956	2026-09-09 21:32:24.619581	\N	\N	f
107	3	Goods worth Rs.1000 bought from Mohan have remained unrecorded	RULE	t	2026-09-09 12:27:30.718691	2026-09-09 21:34:22.801302	\N	\N	f
108	3	Goods worth Rs.1200 returned to Pavan Kumar have remained unrecorded so far.	RULE	t	2026-09-09 12:27:30.729225	2026-09-09 21:35:54.875361	\N	\N	f
109	3	Payment of salary to Varshini not passed through books at all	RULE	t	2026-09-09 12:27:30.736739	2026-09-09 21:37:29.661667	\N	\N	f
\.


--
-- TOC entry 5467 (class 0 OID 30346)
-- Dependencies: 267
-- Data for Name: table_headers; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.table_headers (header_id, name, active_row, created_at, row_status, updated_at) FROM stdin;
1	Debit Particulars	t	2026-09-08 17:22:39.568955	1	2026-09-08 17:37:51.291428
2	Credit Particulars	t	2026-09-08 17:22:53.833731	1	2026-09-08 17:46:48.626709
3	Transaction	t	2026-09-08 19:07:44.124319	1	2026-09-08 19:07:44.125364
4	Asset Side	t	2026-09-08 19:07:57.309896	1	2026-09-08 19:07:57.309896
5	Liabilities Side	t	2026-09-08 19:08:13.962594	1	2026-09-08 19:08:13.962594
6	Adjustments	t	2026-09-08 19:08:23.817082	1	2026-09-08 19:08:23.817082
\.


--
-- TOC entry 5469 (class 0 OID 30356)
-- Dependencies: 269
-- Data for Name: table_names; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.table_names (table_name_id, name, active_row, created_at, row_status, updated_at) FROM stdin;
1	Trading Account	t	2026-09-08 17:17:49.897292	1	2026-09-08 17:17:49.897292
2	Balance Sheet	t	2026-09-08 17:18:25.698698	1	2026-09-08 17:18:25.699686
3	Profit & Loss Account	t	2026-09-08 17:22:14.517227	1	2026-09-08 17:22:14.519397
4	Furniture A/c	t	2026-09-09 10:24:40.838926	1	2026-09-09 10:24:40.839928
5	Cash A/c	t	2026-09-09 10:24:46.920457	1	2026-09-09 10:24:46.920457
6	Machinery A/c	t	2026-09-09 10:24:57.278362	1	2026-09-09 10:24:57.278362
7	Computer A/c	t	2026-09-09 10:25:36.691748	1	2026-09-09 10:25:36.691748
8	Building A/c	t	2026-09-09 10:25:54.919814	1	2026-09-09 10:25:54.919814
9	Bank A/c	t	2026-09-09 10:26:21.20932	1	2026-09-09 10:26:21.20932
10	Capital A/c	t	2026-09-09 10:26:27.875741	1	2026-09-09 10:26:27.876247
11	Shankar A/c	t	2026-09-09 10:26:52.622454	1	2026-09-09 10:26:52.622454
12	Vaishali A/c	t	2026-09-09 10:27:03.989556	1	2026-09-09 10:27:03.989556
13	Drawings A/c	t	2026-09-09 10:27:18.374138	1	2026-09-09 10:27:18.374138
14	Rent A/c	t	2026-09-09 10:27:50.666311	1	2026-09-09 10:27:50.666311
15	Salaries A/c	t	2026-09-09 10:28:00.448177	1	2026-09-09 10:28:00.448177
16	Advertisement A/c	t	2026-09-09 10:28:08.982599	1	2026-09-09 10:28:08.982599
17	Interest A/c	t	2026-09-09 10:28:17.506644	1	2026-09-09 10:28:17.506644
18	Sales A/c	t	2026-09-09 10:28:27.032113	1	2026-09-09 10:28:27.032113
19	Commission received A/c	t	2026-09-09 10:28:37.532166	1	2026-09-09 10:28:37.532166
20	Priya A/c	t	2026-09-09 10:28:58.035356	1	2026-09-09 10:28:58.035356
21	Discount allowed A/c	t	2026-09-09 10:29:07.501495	1	2026-09-09 10:29:07.501495
22	Rahul A/c	t	2026-09-09 10:29:13.386388	1	2026-09-09 10:29:13.386388
23	Vinay A/c	t	2026-09-09 10:29:21.800861	1	2026-09-09 10:29:21.800861
24	Discount received A/c	t	2026-09-09 10:29:28.261561	1	2026-09-09 10:29:28.261561
25	Bad debts A/c	t	2026-09-09 10:29:37.473204	1	2026-09-09 10:29:37.473204
26	Sujatha A/c	t	2026-09-09 10:29:43.545656	1	2026-09-09 10:29:43.545656
27	Purchases A/c	t	2026-09-09 10:30:06.557804	1	2026-09-09 10:30:06.557804
28	Anivish A/c	t	2026-09-09 10:30:12.450689	1	2026-09-09 10:30:12.450689
29	Raju A/c	t	2026-09-09 10:30:33.835915	1	2026-09-09 10:30:33.835915
30	Rithika A/c	t	2026-09-09 10:30:50.114582	1	2026-09-09 10:30:50.114582
31	Sohail A/c	t	2026-09-09 10:31:03.193462	1	2026-09-09 10:31:03.193462
32	Suspense A/c	t	2026-09-09 17:58:25.090995	1	2026-09-09 17:58:25.092994
36	Returns A/C	t	2026-09-09 18:20:53.852697	1	2026-09-09 18:20:53.852697
38	Sales Returnd A/C	t	2026-09-09 18:20:53.863234	1	2026-09-09 18:20:53.863234
39	Lalitha A/C	t	2026-09-09 18:20:53.869234	1	2026-09-09 18:20:53.869234
40	Amala A/C	t	2026-09-09 18:20:53.87275	1	2026-09-09 18:20:53.87275
41	Rajesh A/c	t	2026-09-09 18:20:53.878102	1	2026-09-09 18:20:53.878102
42	General Expenses A/c	t	2026-09-09 18:20:53.881108	1	2026-09-09 18:20:53.881108
43	Ashok A/c	t	2026-09-09 18:20:53.889622	1	2026-09-09 18:20:53.889622
44	Ramesh A/c	t	2026-09-09 18:20:53.893625	1	2026-09-09 18:20:53.893625
45	Amar A/c	t	2026-09-09 18:20:53.897141	1	2026-09-09 18:20:53.897141
46	Sharath A/c	t	2026-09-09 18:20:53.901376	1	2026-09-09 18:20:53.901376
47	Kiran A/c	t	2026-09-09 18:20:53.911165	1	2026-09-09 18:20:53.911165
48	Mukesh A/c	t	2026-09-09 18:20:53.916321	1	2026-09-09 18:20:53.916321
49	Rafi A/c	t	2026-09-09 18:20:53.926507	1	2026-09-09 18:20:53.926507
50	Rent Received A/c	t	2026-09-09 18:20:53.931141	1	2026-09-09 18:20:53.931141
51	Sandeep A/c	t	2026-09-09 18:20:53.934129	1	2026-09-09 18:20:53.934129
52	Sudheer A/c	t	2026-09-09 18:20:53.939799	1	2026-09-09 18:20:53.939799
53	Commission A/c	t	2026-09-09 18:20:53.943896	1	2026-09-09 18:20:53.943896
54	Mohan A/c	t	2026-09-09 18:20:53.948998	1	2026-09-09 18:20:53.948998
55	Prabhas A/c	t	2026-09-09 18:20:53.95434	1	2026-09-09 18:20:53.95434
56	Pradeep A/c	t	2026-09-09 18:20:53.959318	1	2026-09-09 18:20:53.959318
57	Ram A/c	t	2026-09-09 18:20:53.964826	1	2026-09-09 18:20:53.964826
58	Shyam A/c	t	2026-09-09 18:20:53.967826	1	2026-09-09 18:20:53.967826
59	Srinu A/c	t	2026-09-09 18:20:53.972171	1	2026-09-09 18:20:53.972171
60	Mr. Murali A/c	t	2026-09-09 18:20:53.975338	1	2026-09-09 18:20:53.975338
61	Repairs A/c	t	2026-09-09 18:20:53.980339	1	2026-09-09 18:20:53.980339
62	Trade Expenses A/c	t	2026-09-09 18:20:53.984553	1	2026-09-09 18:20:53.984553
63	Typewritter A/c	t	2026-09-09 18:20:53.987552	1	2026-09-09 18:20:53.987552
64	Pavankumar A/c	t	2026-09-09 18:20:53.995062	1	2026-09-09 18:20:53.995062
65	Salary A/c	t	2026-09-09 18:20:54.000062	1	2026-09-09 18:20:54.000062
67	Sudha A/c	t	2026-09-09 18:20:54.007595	1	2026-09-09 18:20:54.007595
34	Purchase returns A/C	t	2026-09-09 18:20:53.843997	1	2026-09-09 18:23:53.549947
66	Sales Return A/c	t	2026-09-09 18:20:54.003065	1	2026-09-09 18:31:26.191473
68	Office Expenses A/c	t	2026-09-09 21:05:01.851834	1	2026-09-09 21:05:01.851834
\.


--
-- TOC entry 5450 (class 0 OID 30244)
-- Dependencies: 250
-- Data for Name: topic; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.topic (topic_id, topic_name, active_row, created_at, row_status, order_of, updated_at, course_id, chapter_id, subject_id) FROM stdin;
1	Easy Model Questions	t	2026-09-08 17:11:14.509298	1	1	2026-09-08 17:11:14.509298	1	1	3
4	Easy Model Questions	t	2026-09-08 17:12:34.726966	1	1	2026-09-08 17:12:34.726966	1	2	3
5	Easy Model Questions	t	2026-09-08 17:12:50.335774	1	1	2026-09-08 17:12:50.335774	1	3	3
6	Easy Model Questions	t	2026-09-08 17:12:58.867518	1	1	2026-09-08 17:12:58.867518	1	4	3
7	Easy Model Questions	t	2026-09-08 17:13:06.125517	1	1	2026-09-08 17:13:06.125517	1	5	3
8	Easy Model Questions	t	2026-09-08 17:13:12.217987	1	1	2026-09-08 17:13:12.217987	1	6	3
9	Mcq Questions	t	2026-09-08 17:14:14.776413	1	1	2026-09-08 17:14:14.776413	1	1	3
10	Mcq Questions	t	2026-09-08 17:14:27.600988	1	1	2026-09-08 17:14:27.600988	1	2	3
11	Mcq Questions	t	2026-09-08 17:14:33.809565	1	1	2026-09-08 17:14:33.809565	1	3	3
\.


--
-- TOC entry 5471 (class 0 OID 30366)
-- Dependencies: 271
-- Data for Name: user_subscriptions; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.user_subscriptions (subscription_id, user_id, plan_id, course_id, starts_at, expires_at, active, practice_questions_used, mock_tests_used, exam_attempts_used) FROM stdin;
\.


--
-- TOC entry 5473 (class 0 OID 30928)
-- Dependencies: 273
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.users (user_id, name, designation, address, employee_id, student_id, course_id, college_id, branch_id, section_id, email, phone_number, password, guardian_name, guardian_phone_number, role_id, google_id, profile_picture, login_type, active_row, created_at, updated_at) FROM stdin;
2	Raghavendra	Super Admin	Hyderabad	1001	\N	\N	\N	\N	\N	raghav@gmail.com	9876543210	$2a$12$4XvQyfxwQxkLuILE6xhiWuz8PSlgDQNE3TfZpTqGhq4TyVvQgymJG	\N	\N	1	\N	\N	NORMAL	t	2026-08-22 17:19:37.86439	2026-08-22 17:19:37.86439
3	Charan Vamshi	BranchAdmin1	Hyderabad	2001	\N	\N	1	1	\N	charan@gmail.com	7894561258	$2a$12$TEF.sHYp2NDCXvaGPUMq1uTmi3Ogh0ViLay9HhOqBC5wleXA./yZi	\N	\N	2	\N	\N	NORMAL	t	2026-08-24 11:05:37.934636	2026-08-24 11:05:37.934636
4	Shiva	\N	Karmanghat	\N	3001	\N	1	1	1	shiva@gmail.com	8521463975	$2a$12$5UVX55esUV0JPzPn36gBl.OuxFRdpKVcYVPrefoml6Gpu9ldr.CNq	Ram	7458125639	3	\N	\N	NORMAL	t	2026-08-24 11:26:53.290891	2026-08-24 11:26:53.290891
5	Vamshidhar	\N	Karmanghat	\N	\N	\N	\N	\N	\N	vamshi@gmail.com	7458213658	$2a$12$Keb/d4p2Fw6xGFmQ9XrKmuHMz.bjf9wy21BRwy4PzAQCiskRvTnae	\N	\N	4	\N	\N	NORMAL	t	2026-08-24 11:55:47.137254	2026-08-24 11:55:47.137254
6	Raghavendra 6736	\N	\N	\N	\N	\N	\N	\N	\N	narsapuramragava@gmail.com	\N	\N	\N	\N	4	101317339325084238483	https://lh3.googleusercontent.com/a/ACg8ocJsv0pYahtWRxFXli_JFBWQLKhbF2zR5oA6vlz_hHkx_2xcWPVW=s96-c	GOOGLE	t	2026-08-24 12:37:23.297034	2026-08-24 12:37:23.297034
7	Charan Vamshi Yerukala	\N	\N	\N	\N	\N	\N	\N	\N	charanvamshi91@gmail.com	\N	\N	\N	\N	4	115049237950217908737	https://lh3.googleusercontent.com/a/ACg8ocLmZyfRX8arBVr6KYjpJuao4DwMeQ-PihTJ_BHUUvtadtLt_g=s96-c	GOOGLE	t	2026-08-24 13:21:31.426877	2026-08-24 13:21:31.426877
1	Abhimanyu	\N	\N	\N	\N	\N	\N	\N	\N	abhi@gmail.com	\N	hi	\N	\N	1	\N	\N	NORMAL	f	2026-08-22 00:09:55.556023	2026-08-24 22:16:49.282891
8	Prem	Super Admin	Hyderabad	1002	\N	\N	\N	\N	\N	prem@gmail.com	9876458214	$2a$12$MR4pGQJE6OB2OPzaioSoouspfacTabXoxTKd3.dSiav3NlPpLyAb2	\N	\N	1	\N	\N	NORMAL	t	2026-08-27 10:48:51.957487	2026-08-27 10:48:51.957487
\.


--
-- TOC entry 5508 (class 0 OID 0)
-- Dependencies: 221
-- Name: answer_events_answer_event_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.answer_events_answer_event_id_seq', 2, true);


--
-- TOC entry 5509 (class 0 OID 0)
-- Dependencies: 223
-- Name: attendance_attendance_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.attendance_attendance_id_seq', 1, false);


--
-- TOC entry 5510 (class 0 OID 0)
-- Dependencies: 225
-- Name: branch_branch_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.branch_branch_id_seq', 3, true);


--
-- TOC entry 5511 (class 0 OID 0)
-- Dependencies: 227
-- Name: chapters_chapter_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.chapters_chapter_id_seq', 6, true);


--
-- TOC entry 5512 (class 0 OID 0)
-- Dependencies: 229
-- Name: college_college_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.college_college_id_seq', 1, true);


--
-- TOC entry 5513 (class 0 OID 0)
-- Dependencies: 231
-- Name: courses_course_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.courses_course_id_seq', 2, true);


--
-- TOC entry 5514 (class 0 OID 0)
-- Dependencies: 233
-- Name: exam_answers_answer_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.exam_answers_answer_id_seq', 1, true);


--
-- TOC entry 5515 (class 0 OID 0)
-- Dependencies: 236
-- Name: exam_questions_exam_question_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.exam_questions_exam_question_id_seq', 1, false);


--
-- TOC entry 5516 (class 0 OID 0)
-- Dependencies: 238
-- Name: exam_questions_question_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.exam_questions_question_id_seq', 150, true);


--
-- TOC entry 5517 (class 0 OID 0)
-- Dependencies: 240
-- Name: exam_result_exam_result_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.exam_result_exam_result_id_seq', 1, false);


--
-- TOC entry 5518 (class 0 OID 0)
-- Dependencies: 243
-- Name: exams_exam_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.exams_exam_id_seq', 1, false);


--
-- TOC entry 5519 (class 0 OID 0)
-- Dependencies: 244
-- Name: exams_exam_id_seq1; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.exams_exam_id_seq1', 1, false);


--
-- TOC entry 5520 (class 0 OID 0)
-- Dependencies: 246
-- Name: mcq_options_option_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.mcq_options_option_id_seq', 80, true);


--
-- TOC entry 5521 (class 0 OID 0)
-- Dependencies: 249
-- Name: plan_courses_plan_course_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.plan_courses_plan_course_id_seq', 1, false);


--
-- TOC entry 5522 (class 0 OID 0)
-- Dependencies: 251
-- Name: question_categories_category_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.question_categories_category_id_seq', 11, true);


--
-- TOC entry 5523 (class 0 OID 0)
-- Dependencies: 253
-- Name: question_type_question_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.question_type_question_type_id_seq', 5, true);


--
-- TOC entry 5524 (class 0 OID 0)
-- Dependencies: 254
-- Name: questions_question_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.questions_question_id_seq', 42, true);


--
-- TOC entry 5525 (class 0 OID 0)
-- Dependencies: 256
-- Name: roles_role_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.roles_role_id_seq', 9, true);


--
-- TOC entry 5526 (class 0 OID 0)
-- Dependencies: 258
-- Name: rule_engines_rule_engine_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.rule_engines_rule_engine_id_seq', 107, true);


--
-- TOC entry 5527 (class 0 OID 0)
-- Dependencies: 260
-- Name: section_section_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.section_section_id_seq', 1, false);


--
-- TOC entry 5528 (class 0 OID 0)
-- Dependencies: 262
-- Name: subject_subject_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.subject_subject_id_seq', 3, true);


--
-- TOC entry 5529 (class 0 OID 0)
-- Dependencies: 264
-- Name: subscription_plans_plan_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.subscription_plans_plan_id_seq', 4, true);


--
-- TOC entry 5530 (class 0 OID 0)
-- Dependencies: 266
-- Name: table_attributes_attribute_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.table_attributes_attribute_id_seq', 115, true);


--
-- TOC entry 5531 (class 0 OID 0)
-- Dependencies: 268
-- Name: table_headers_header_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.table_headers_header_id_seq', 6, true);


--
-- TOC entry 5532 (class 0 OID 0)
-- Dependencies: 270
-- Name: table_names_table_name_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.table_names_table_name_id_seq', 68, true);


--
-- TOC entry 5533 (class 0 OID 0)
-- Dependencies: 272
-- Name: user_subscriptions_subscription_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.user_subscriptions_subscription_id_seq', 1, false);


--
-- TOC entry 5534 (class 0 OID 0)
-- Dependencies: 274
-- Name: users_user_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.users_user_id_seq', 8, true);


--
-- TOC entry 5146 (class 2606 OID 30425)
-- Name: answer_events answer_events_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.answer_events
    ADD CONSTRAINT answer_events_pkey PRIMARY KEY (answer_event_id);


--
-- TOC entry 5148 (class 2606 OID 30427)
-- Name: attendance attendance_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.attendance
    ADD CONSTRAINT attendance_pkey PRIMARY KEY (attendance_id);


--
-- TOC entry 5150 (class 2606 OID 30429)
-- Name: branch branch_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.branch
    ADD CONSTRAINT branch_pkey PRIMARY KEY (branch_id);


--
-- TOC entry 5152 (class 2606 OID 30431)
-- Name: chapters chapters_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.chapters
    ADD CONSTRAINT chapters_pkey PRIMARY KEY (chapter_id);


--
-- TOC entry 5154 (class 2606 OID 30433)
-- Name: college college_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.college
    ADD CONSTRAINT college_pkey PRIMARY KEY (college_id);


--
-- TOC entry 5156 (class 2606 OID 30435)
-- Name: courses courses_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.courses
    ADD CONSTRAINT courses_pkey PRIMARY KEY (course_id);


--
-- TOC entry 5158 (class 2606 OID 30437)
-- Name: question_answers exam_answers_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.question_answers
    ADD CONSTRAINT exam_answers_pkey PRIMARY KEY (answer_id);


--
-- TOC entry 5160 (class 2606 OID 30439)
-- Name: exam_chapters exam_chapters_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.exam_chapters
    ADD CONSTRAINT exam_chapters_pkey PRIMARY KEY (exam_id, chapter_id);


--
-- TOC entry 5166 (class 2606 OID 30441)
-- Name: question_attributes exam_questions_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.question_attributes
    ADD CONSTRAINT exam_questions_pkey PRIMARY KEY (question_attribute_id);


--
-- TOC entry 5162 (class 2606 OID 30443)
-- Name: exam_questions exam_questions_pkey1; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.exam_questions
    ADD CONSTRAINT exam_questions_pkey1 PRIMARY KEY (exam_question_id);


--
-- TOC entry 5168 (class 2606 OID 30445)
-- Name: exam_result exam_result_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.exam_result
    ADD CONSTRAINT exam_result_pkey PRIMARY KEY (exam_result_id);


--
-- TOC entry 5172 (class 2606 OID 30447)
-- Name: questions exams_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.questions
    ADD CONSTRAINT exams_pkey PRIMARY KEY (question_id);


--
-- TOC entry 5170 (class 2606 OID 30449)
-- Name: exams exams_pkey1; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.exams
    ADD CONSTRAINT exams_pkey1 PRIMARY KEY (exam_id);


--
-- TOC entry 5174 (class 2606 OID 30451)
-- Name: mcq_options mcq_options_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.mcq_options
    ADD CONSTRAINT mcq_options_pkey PRIMARY KEY (option_id);


--
-- TOC entry 5178 (class 2606 OID 30453)
-- Name: mcq_questions mcq_questions_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.mcq_questions
    ADD CONSTRAINT mcq_questions_pkey PRIMARY KEY (question_id);


--
-- TOC entry 5181 (class 2606 OID 30455)
-- Name: plan_courses plan_courses_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.plan_courses
    ADD CONSTRAINT plan_courses_pkey PRIMARY KEY (plan_course_id);


--
-- TOC entry 5185 (class 2606 OID 30457)
-- Name: topic question_categories_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.topic
    ADD CONSTRAINT question_categories_pkey PRIMARY KEY (topic_id);


--
-- TOC entry 5187 (class 2606 OID 30459)
-- Name: question_type question_type_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.question_type
    ADD CONSTRAINT question_type_pkey PRIMARY KEY (question_type_id);


--
-- TOC entry 5189 (class 2606 OID 30461)
-- Name: roles roles_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT roles_pkey PRIMARY KEY (role_id);


--
-- TOC entry 5191 (class 2606 OID 30463)
-- Name: roles roles_role_name_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT roles_role_name_key UNIQUE (role_name);


--
-- TOC entry 5193 (class 2606 OID 30465)
-- Name: rule_engines rule_engines_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.rule_engines
    ADD CONSTRAINT rule_engines_pkey PRIMARY KEY (rule_engine_id);


--
-- TOC entry 5195 (class 2606 OID 30467)
-- Name: section section_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.section
    ADD CONSTRAINT section_pkey PRIMARY KEY (section_id);


--
-- TOC entry 5197 (class 2606 OID 30469)
-- Name: subject subject_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.subject
    ADD CONSTRAINT subject_pkey PRIMARY KEY (subject_id);


--
-- TOC entry 5199 (class 2606 OID 30471)
-- Name: subscription_plans subscription_plans_name_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.subscription_plans
    ADD CONSTRAINT subscription_plans_name_key UNIQUE (name);


--
-- TOC entry 5201 (class 2606 OID 30473)
-- Name: subscription_plans subscription_plans_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.subscription_plans
    ADD CONSTRAINT subscription_plans_pkey PRIMARY KEY (plan_id);


--
-- TOC entry 5203 (class 2606 OID 30475)
-- Name: table_attributes table_attributes_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.table_attributes
    ADD CONSTRAINT table_attributes_pkey PRIMARY KEY (attribute_id);


--
-- TOC entry 5205 (class 2606 OID 30477)
-- Name: table_headers table_headers_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.table_headers
    ADD CONSTRAINT table_headers_pkey PRIMARY KEY (header_id);


--
-- TOC entry 5207 (class 2606 OID 30479)
-- Name: table_names table_names_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.table_names
    ADD CONSTRAINT table_names_pkey PRIMARY KEY (table_name_id);


--
-- TOC entry 5164 (class 2606 OID 30481)
-- Name: exam_questions uk_exam_question; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.exam_questions
    ADD CONSTRAINT uk_exam_question UNIQUE (exam_id, question_id);


--
-- TOC entry 5176 (class 2606 OID 30483)
-- Name: mcq_options uq_mcq_option_order; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.mcq_options
    ADD CONSTRAINT uq_mcq_option_order UNIQUE (question_id, option_order);


--
-- TOC entry 5183 (class 2606 OID 30485)
-- Name: plan_courses uq_plan_courses; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.plan_courses
    ADD CONSTRAINT uq_plan_courses UNIQUE (plan_id, course_id);


--
-- TOC entry 5211 (class 2606 OID 30487)
-- Name: user_subscriptions user_subscriptions_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_subscriptions
    ADD CONSTRAINT user_subscriptions_pkey PRIMARY KEY (subscription_id);


--
-- TOC entry 5213 (class 2606 OID 30944)
-- Name: users users_email_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_email_key UNIQUE (email);


--
-- TOC entry 5215 (class 2606 OID 30946)
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (user_id);


--
-- TOC entry 5179 (class 1259 OID 30492)
-- Name: idx_plan_courses_course_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_plan_courses_course_id ON public.plan_courses USING btree (course_id);


--
-- TOC entry 5208 (class 1259 OID 30493)
-- Name: idx_user_subscriptions_course_active; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_user_subscriptions_course_active ON public.user_subscriptions USING btree (course_id, active);


--
-- TOC entry 5209 (class 1259 OID 30494)
-- Name: idx_user_subscriptions_user_active; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_user_subscriptions_user_active ON public.user_subscriptions USING btree (user_id, active);


--
-- TOC entry 5223 (class 2606 OID 30495)
-- Name: question_answers exam_answers_attribute_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.question_answers
    ADD CONSTRAINT exam_answers_attribute_id_fkey FOREIGN KEY (attribute_id) REFERENCES public.table_attributes(attribute_id);


--
-- TOC entry 5224 (class 2606 OID 30500)
-- Name: question_answers exam_answers_exam_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.question_answers
    ADD CONSTRAINT exam_answers_exam_id_fkey FOREIGN KEY (question_id) REFERENCES public.questions(question_id);


--
-- TOC entry 5225 (class 2606 OID 30505)
-- Name: question_answers exam_answers_header_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.question_answers
    ADD CONSTRAINT exam_answers_header_id_fkey FOREIGN KEY (header_id) REFERENCES public.table_headers(header_id);


--
-- TOC entry 5226 (class 2606 OID 30510)
-- Name: question_answers exam_answers_table_name_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.question_answers
    ADD CONSTRAINT exam_answers_table_name_id_fkey FOREIGN KEY (table_name_id) REFERENCES public.table_names(table_name_id);


--
-- TOC entry 5233 (class 2606 OID 30515)
-- Name: question_attributes exam_questions_attribute_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.question_attributes
    ADD CONSTRAINT exam_questions_attribute_id_fkey FOREIGN KEY (attribute_id) REFERENCES public.table_attributes(attribute_id);


--
-- TOC entry 5234 (class 2606 OID 30520)
-- Name: question_attributes exam_questions_exam_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.question_attributes
    ADD CONSTRAINT exam_questions_exam_id_fkey FOREIGN KEY (question_id) REFERENCES public.questions(question_id);


--
-- TOC entry 5235 (class 2606 OID 30525)
-- Name: question_attributes exam_questions_header_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.question_attributes
    ADD CONSTRAINT exam_questions_header_id_fkey FOREIGN KEY (header_id) REFERENCES public.table_headers(header_id);


--
-- TOC entry 5216 (class 2606 OID 30530)
-- Name: answer_events fk_answer_events_attribute; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.answer_events
    ADD CONSTRAINT fk_answer_events_attribute FOREIGN KEY (attribute_id) REFERENCES public.table_attributes(attribute_id);


--
-- TOC entry 5217 (class 2606 OID 30535)
-- Name: answer_events fk_answer_events_question; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.answer_events
    ADD CONSTRAINT fk_answer_events_question FOREIGN KEY (question_id) REFERENCES public.questions(question_id);


--
-- TOC entry 5268 (class 2606 OID 30545)
-- Name: table_attributes fk_attribute_header; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.table_attributes
    ADD CONSTRAINT fk_attribute_header FOREIGN KEY (header_id) REFERENCES public.table_headers(header_id);


--
-- TOC entry 5218 (class 2606 OID 30550)
-- Name: branch fk_branch_college; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.branch
    ADD CONSTRAINT fk_branch_college FOREIGN KEY (college_id) REFERENCES public.college(college_id) ON DELETE CASCADE;


--
-- TOC entry 5219 (class 2606 OID 30555)
-- Name: chapters fk_chapter_course; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.chapters
    ADD CONSTRAINT fk_chapter_course FOREIGN KEY (course_id) REFERENCES public.courses(course_id) ON UPDATE CASCADE ON DELETE RESTRICT;


--
-- TOC entry 5220 (class 2606 OID 30560)
-- Name: chapters fk_chapters_subject; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.chapters
    ADD CONSTRAINT fk_chapters_subject FOREIGN KEY (subject_id) REFERENCES public.subject(subject_id);


--
-- TOC entry 5221 (class 2606 OID 30565)
-- Name: courses fk_course_branch; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.courses
    ADD CONSTRAINT fk_course_branch FOREIGN KEY (branch_id) REFERENCES public.branch(branch_id);


--
-- TOC entry 5222 (class 2606 OID 30570)
-- Name: courses fk_courses_college; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.courses
    ADD CONSTRAINT fk_courses_college FOREIGN KEY (college_id) REFERENCES public.college(college_id);


--
-- TOC entry 5237 (class 2606 OID 30580)
-- Name: exams fk_exam_branch; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.exams
    ADD CONSTRAINT fk_exam_branch FOREIGN KEY (branch_id) REFERENCES public.branch(branch_id);


--
-- TOC entry 5241 (class 2606 OID 30585)
-- Name: questions fk_exam_category; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.questions
    ADD CONSTRAINT fk_exam_category FOREIGN KEY (topic_id) REFERENCES public.topic(topic_id) ON UPDATE CASCADE ON DELETE RESTRICT;


--
-- TOC entry 5242 (class 2606 OID 30590)
-- Name: questions fk_exam_chapter; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.questions
    ADD CONSTRAINT fk_exam_chapter FOREIGN KEY (chapter_id) REFERENCES public.chapters(chapter_id) ON UPDATE CASCADE ON DELETE RESTRICT;


--
-- TOC entry 5229 (class 2606 OID 30595)
-- Name: exam_chapters fk_exam_chapters_chapter; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.exam_chapters
    ADD CONSTRAINT fk_exam_chapters_chapter FOREIGN KEY (chapter_id) REFERENCES public.chapters(chapter_id) ON DELETE RESTRICT;


--
-- TOC entry 5230 (class 2606 OID 30600)
-- Name: exam_chapters fk_exam_chapters_exam; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.exam_chapters
    ADD CONSTRAINT fk_exam_chapters_exam FOREIGN KEY (exam_id) REFERENCES public.exams(exam_id) ON DELETE CASCADE;


--
-- TOC entry 5238 (class 2606 OID 30605)
-- Name: exams fk_exam_college; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.exams
    ADD CONSTRAINT fk_exam_college FOREIGN KEY (college_id) REFERENCES public.college(college_id);


--
-- TOC entry 5239 (class 2606 OID 30610)
-- Name: exams fk_exam_course; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.exams
    ADD CONSTRAINT fk_exam_course FOREIGN KEY (course_id) REFERENCES public.courses(course_id);


--
-- TOC entry 5231 (class 2606 OID 30615)
-- Name: exam_questions fk_exam_questions_exam; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.exam_questions
    ADD CONSTRAINT fk_exam_questions_exam FOREIGN KEY (exam_id) REFERENCES public.exams(exam_id) ON DELETE CASCADE;


--
-- TOC entry 5232 (class 2606 OID 30620)
-- Name: exam_questions fk_exam_questions_question; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.exam_questions
    ADD CONSTRAINT fk_exam_questions_question FOREIGN KEY (question_id) REFERENCES public.questions(question_id) ON DELETE RESTRICT;


--
-- TOC entry 5236 (class 2606 OID 30625)
-- Name: exam_result fk_exam_result_exams; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.exam_result
    ADD CONSTRAINT fk_exam_result_exams FOREIGN KEY (exam_id) REFERENCES public.exams(exam_id);


--
-- TOC entry 5240 (class 2606 OID 30635)
-- Name: exams fk_exam_section; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.exams
    ADD CONSTRAINT fk_exam_section FOREIGN KEY (section_id) REFERENCES public.section(section_id);


--
-- TOC entry 5247 (class 2606 OID 30640)
-- Name: mcq_questions fk_mcq_questions_question_type; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.mcq_questions
    ADD CONSTRAINT fk_mcq_questions_question_type FOREIGN KEY (question_type_id) REFERENCES public.question_type(question_type_id);


--
-- TOC entry 5227 (class 2606 OID 30645)
-- Name: question_answers fk_question_answers_question; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.question_answers
    ADD CONSTRAINT fk_question_answers_question FOREIGN KEY (question_id) REFERENCES public.questions(question_id);


--
-- TOC entry 5250 (class 2606 OID 30650)
-- Name: topic fk_question_categories_chapter; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.topic
    ADD CONSTRAINT fk_question_categories_chapter FOREIGN KEY (chapter_id) REFERENCES public.chapters(chapter_id);


--
-- TOC entry 5251 (class 2606 OID 30655)
-- Name: topic fk_question_categories_course; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.topic
    ADD CONSTRAINT fk_question_categories_course FOREIGN KEY (course_id) REFERENCES public.courses(course_id);


--
-- TOC entry 5252 (class 2606 OID 30660)
-- Name: topic fk_question_categories_subject; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.topic
    ADD CONSTRAINT fk_question_categories_subject FOREIGN KEY (subject_id) REFERENCES public.subject(subject_id);


--
-- TOC entry 5243 (class 2606 OID 30665)
-- Name: questions fk_questions_course; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.questions
    ADD CONSTRAINT fk_questions_course FOREIGN KEY (course_id) REFERENCES public.courses(course_id);


--
-- TOC entry 5244 (class 2606 OID 30670)
-- Name: questions fk_questions_question_type; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.questions
    ADD CONSTRAINT fk_questions_question_type FOREIGN KEY (question_type_id) REFERENCES public.question_type(question_type_id);


--
-- TOC entry 5245 (class 2606 OID 30675)
-- Name: questions fk_questions_subject; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.questions
    ADD CONSTRAINT fk_questions_subject FOREIGN KEY (subject_id) REFERENCES public.subject(subject_id);


--
-- TOC entry 5253 (class 2606 OID 30680)
-- Name: rule_engines fk_rule_engine_attribute; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.rule_engines
    ADD CONSTRAINT fk_rule_engine_attribute FOREIGN KEY (attribute_id) REFERENCES public.table_attributes(attribute_id);


--
-- TOC entry 5264 (class 2606 OID 30685)
-- Name: section fk_section_branch; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.section
    ADD CONSTRAINT fk_section_branch FOREIGN KEY (branch_id) REFERENCES public.branch(branch_id);


--
-- TOC entry 5265 (class 2606 OID 30690)
-- Name: section fk_section_college; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.section
    ADD CONSTRAINT fk_section_college FOREIGN KEY (college_id) REFERENCES public.college(college_id);


--
-- TOC entry 5266 (class 2606 OID 30695)
-- Name: section fk_section_course; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.section
    ADD CONSTRAINT fk_section_course FOREIGN KEY (course_id) REFERENCES public.courses(course_id) ON DELETE CASCADE;


--
-- TOC entry 5267 (class 2606 OID 30700)
-- Name: subject fk_subject_courses; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.subject
    ADD CONSTRAINT fk_subject_courses FOREIGN KEY (course_id) REFERENCES public.courses(course_id);


--
-- TOC entry 5271 (class 2606 OID 30957)
-- Name: users fk_user_course; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT fk_user_course FOREIGN KEY (course_id) REFERENCES public.courses(course_id);


--
-- TOC entry 5272 (class 2606 OID 30962)
-- Name: users fk_user_role; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT fk_user_role FOREIGN KEY (role_id) REFERENCES public.roles(role_id);


--
-- TOC entry 5228 (class 2606 OID 30730)
-- Name: question_answers fksgxfjyk5slb4tkwo619e1vrbo; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.question_answers
    ADD CONSTRAINT fksgxfjyk5slb4tkwo619e1vrbo FOREIGN KEY (pair_attribute_id) REFERENCES public.table_attributes(attribute_id);


--
-- TOC entry 5246 (class 2606 OID 30735)
-- Name: mcq_options mcq_options_question_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.mcq_options
    ADD CONSTRAINT mcq_options_question_id_fkey FOREIGN KEY (question_id) REFERENCES public.mcq_questions(question_id) ON DELETE CASCADE;


--
-- TOC entry 5248 (class 2606 OID 30740)
-- Name: plan_courses plan_courses_course_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.plan_courses
    ADD CONSTRAINT plan_courses_course_id_fkey FOREIGN KEY (course_id) REFERENCES public.courses(course_id);


--
-- TOC entry 5249 (class 2606 OID 30745)
-- Name: plan_courses plan_courses_plan_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.plan_courses
    ADD CONSTRAINT plan_courses_plan_id_fkey FOREIGN KEY (plan_id) REFERENCES public.subscription_plans(plan_id);


--
-- TOC entry 5254 (class 2606 OID 30750)
-- Name: rule_engines rule_engines_chapter_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.rule_engines
    ADD CONSTRAINT rule_engines_chapter_id_fkey FOREIGN KEY (chapter_id) REFERENCES public.chapters(chapter_id);


--
-- TOC entry 5255 (class 2606 OID 30755)
-- Name: rule_engines rule_engines_header1_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.rule_engines
    ADD CONSTRAINT rule_engines_header1_id_fkey FOREIGN KEY (header1_id) REFERENCES public.table_headers(header_id);


--
-- TOC entry 5256 (class 2606 OID 30760)
-- Name: rule_engines rule_engines_header2_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.rule_engines
    ADD CONSTRAINT rule_engines_header2_id_fkey FOREIGN KEY (header2_id) REFERENCES public.table_headers(header_id);


--
-- TOC entry 5257 (class 2606 OID 30765)
-- Name: rule_engines rule_engines_header3_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.rule_engines
    ADD CONSTRAINT rule_engines_header3_id_fkey FOREIGN KEY (header3_id) REFERENCES public.table_headers(header_id);


--
-- TOC entry 5258 (class 2606 OID 30770)
-- Name: rule_engines rule_engines_header4_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.rule_engines
    ADD CONSTRAINT rule_engines_header4_id_fkey FOREIGN KEY (header4_id) REFERENCES public.table_headers(header_id);


--
-- TOC entry 5259 (class 2606 OID 30775)
-- Name: rule_engines rule_engines_pair_attribute_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.rule_engines
    ADD CONSTRAINT rule_engines_pair_attribute_id_fkey FOREIGN KEY (pair_attribute_id) REFERENCES public.table_attributes(attribute_id);


--
-- TOC entry 5260 (class 2606 OID 30780)
-- Name: rule_engines rule_engines_table1_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.rule_engines
    ADD CONSTRAINT rule_engines_table1_id_fkey FOREIGN KEY (table1_id) REFERENCES public.table_names(table_name_id);


--
-- TOC entry 5261 (class 2606 OID 30785)
-- Name: rule_engines rule_engines_table2_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.rule_engines
    ADD CONSTRAINT rule_engines_table2_id_fkey FOREIGN KEY (table2_id) REFERENCES public.table_names(table_name_id);


--
-- TOC entry 5262 (class 2606 OID 30790)
-- Name: rule_engines rule_engines_table3_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.rule_engines
    ADD CONSTRAINT rule_engines_table3_id_fkey FOREIGN KEY (table3_id) REFERENCES public.table_names(table_name_id);


--
-- TOC entry 5263 (class 2606 OID 30795)
-- Name: rule_engines rule_engines_table4_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.rule_engines
    ADD CONSTRAINT rule_engines_table4_id_fkey FOREIGN KEY (table4_id) REFERENCES public.table_names(table_name_id);


--
-- TOC entry 5269 (class 2606 OID 30800)
-- Name: user_subscriptions user_subscriptions_course_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_subscriptions
    ADD CONSTRAINT user_subscriptions_course_id_fkey FOREIGN KEY (course_id) REFERENCES public.courses(course_id);


--
-- TOC entry 5270 (class 2606 OID 30805)
-- Name: user_subscriptions user_subscriptions_plan_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_subscriptions
    ADD CONSTRAINT user_subscriptions_plan_id_fkey FOREIGN KEY (plan_id) REFERENCES public.subscription_plans(plan_id);


-- Completed on 2026-09-10 10:30:51

--
-- PostgreSQL database dump complete
--

\unrestrict Uij8OcWTHaRxCnRk0zIX57cNqhsYADUvqLihD4sVbEzDuTeGlcpGCmYZuaFb38I

