# Use a imagem oficial do AWS CLI como base
FROM amazon/aws-cli:latest

RUN yum update -y && \
    yum install -y jq nc && \
    yum clean all