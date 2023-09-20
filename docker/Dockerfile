# syntax=docker/dockerfile:1

FROM ubuntu:latest

ENV HOST_IP="127.0.0.1"
ENV VERIF_CODE="12345"

RUN apt update
RUN apt install python3 -y
WORKDIR /app

COPY main.py ./

CMD python3 main.py ${VERIF_CODE} ${HOST_IP}