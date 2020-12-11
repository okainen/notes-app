FROM ubuntu

RUN apt update \
  && apt install -y wget \
  && apt install unzip \
  && apt install netcat -y \
  && rm -rf /var/lib/apt/lists/*

WORKDIR /usr/local/bin

RUN wget https://download.java.net/java/GA/jdk15.0.1/51f4f36ad4ef43e39d0dfdbaf6549e32/9/GPL/openjdk-15.0.1_linux-x64_bin.tar.gz && \
  tar zxvf openjdk-15.0.1_linux-x64_bin.tar.gz && \
  rm openjdk-15.0.1_linux-x64_bin.tar.gz

RUN wget https://github.com/sbt/sbt/releases/download/v1.4.4/sbt-1.4.4.tgz && \
  tar zxvf sbt-1.4.4.tgz \
  && rm sbt-1.4.4.tgz

ENV PATH="/usr/local/bin/sbt/bin:${PATH}"
ENV PATH="/usr/local/bin/jdk-15.0.1/bin:${PATH}"

ENV PORT=80
EXPOSE 80

COPY . /opt/notes-backend

WORKDIR /opt/notes-backend

RUN sbt dist \
  && unzip ./target/universal/notes-app-1.0.zip -d ./target/universal \
  && chmod +x ./target/universal/notes-app-1.0/bin/notes-app

CMD ./target/universal/notes-app-1.0/bin/notes-app
