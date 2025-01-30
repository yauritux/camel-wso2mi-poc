FROM eclipse-temurin:17-jdk

ENV MI_VERSION=4.3.0
ENV MI_HOME=/opt/wso2mi-${MI_VERSION} 
ENV PATH=$MI_HOME/bin:$PATH

RUN groupadd --system wso2group && useradd --system --no-create-home --gid wso2group wso2user

COPY wso2mi-4.3.0.zip /opt

WORKDIR /opt

RUN apt-get update && apt-get install -y unzip && \
    unzip wso2mi-4.3.0.zip -d /opt/ && \
    rm wso2mi-4.3.0.zip

WORKDIR $MI_HOME

RUN chown -R wso2user:wso2group $MI_HOME && chmod -R 755 $MI_HOME

USER wso2user

COPY --chown=wso2user:wso2group ./wso2mi-service-poc_1.0.0.car $MI_HOME/repository/deployment/server/carbonapps/
COPY --chown=wso2user:wso2group ./deployment/deployment.toml $MI_HOME/conf/

WORKDIR $MI_HOME/bin

EXPOSE 8290 9201 9164 8253

ENTRYPOINT [ "./micro-integrator.sh" ]