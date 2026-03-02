FROM ubuntu:latest
LABEL authors="necorch"

ENTRYPOINT ["top", "-b"]