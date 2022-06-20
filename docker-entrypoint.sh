#!/usr/bin/env bash

set -e

#find . -type f -exec dos2unix {} \;

# print all comands to console if DEBUG is set
if [[ ! -z "${DEBUG}" ]]; then
    set -x
fi

# set some helpful variables
export CREATE_DB_SQL='etc/create_database_MySQL.sql'
export SERVICE_PROPERTY_FILE='etc/i5.las2peer.services.mentoringCockpitService.MentoringCockpitService.properties'
export SERVICE_VERSION=$(awk -F "=" '/service.version/ {print $2}' etc/ant_configuration/service.properties)
export SERVICE_NAME=$(awk -F "=" '/service.name/ {print $2}' etc/ant_configuration/service.properties)
export SERVICE_CLASS=$(awk -F "=" '/service.class/ {print $2}' etc/ant_configuration/service.properties)
export SERVICE=${SERVICE_NAME}.${SERVICE_CLASS}@${SERVICE_VERSION}

# check mandatory variables
[[ -z "${LRS_DOMAIN}" ]] && \
    echo "Mandatory variable LRS_DOMAIN is not set. Add -e LRS_DOMAIN=lrsDomain to your arguments." && exit 1
[[ -z "${LRS_AUTH}" ]] && \
    echo "Mandatory variable LRS_AUTH is not set. Add -e LRS_AUTH=lrsAuth to your arguments." && exit 1
[[ -z "${MYSQL_USER}" ]] && \
    echo "Mandatory variable MYSQL_USER is not set. Add -e MYSQL_USER=mysqlUser to your arguments." && exit 1
[[ -z "${MYSQL_PASSWORD}" ]] && \
    echo "Mandatory variable MYSQL_PASSWORD is not set. Add -e MYSQL_PASSWORD=mysqlPassword to your arguments." && exit 1
[[ -z "${MYSQL_HOST}" ]] && \
    echo "Mandatory variable MYSQL_HOST is not set. Add -e MYSQL_HOST=mysqlHost to your arguments." && exit 1
[[ -z "${MYSQL_PORT}" ]] && \
    echo "Mandatory variable MYSQL_PORT is not set. Add -e MYSQL_PORT=mysqlPort to your arguments." && exit 1
[[ -z "${MYSQL_DATABASE}" ]] && \
    echo "Mandatory variable MYSQL_DATABASE is not set. Add -e MYSQL_DATABASE=mysqlDatabase to your arguments." && exit 1
[[ -z "${LRS_CLIENT_URL}" ]] && \
    echo "Mandatory variable LRS_CLIENT_URL is not set. Add -e MYSQL_DATABASE=mysqlDatabase to your arguments." && exit 1
[[ -z "${SPARQL_URL}" ]] && \
    echo "Mandatory variable SPARQL_URL is not set. Add -e MYSQL_DATABASE=mysqlDatabase to your arguments." && exit 1

# optional variables
[[ -z "${SERVICE_PASSPHRASE}" ]] && export SERVICE_PASSPHRASE='mentoring'

# configure service properties
function set_in_service_config {
    sed -i "s#${1}[[:blank:]]*=.*#${1}=${2}#g" ${SERVICE_PROPERTY_FILE}
}
set_in_service_config lrsDomain ${LRS_DOMAIN}
set_in_service_config lrsAuth "${LRS_AUTH}"
set_in_service_config mysqlUser ${MYSQL_USER}
set_in_service_config mysqlPassword ${MYSQL_PASSWORD}
set_in_service_config mysqlHost ${MYSQL_HOST}
set_in_service_config mysqlPort ${MYSQL_PORT}
set_in_service_config mysqlDatabase ${MYSQL_DATABASE}
set_in_service_config lrsClientURL ${LRS_CLIENT_URL}
set_in_service_config sparqlUrl ${SPARQL_URL}


# wait for any bootstrap host to be available
if [[ ! -z "${BOOTSTRAP}" ]]; then
    echo "Waiting for any bootstrap host to become available..."
    for host_port in ${BOOTSTRAP//,/ }; do
        arr_host_port=(${host_port//:/ })
        host=${arr_host_port[0]}
        port=${arr_host_port[1]}
        if { </dev/tcp/${host}/${port}; } 2>/dev/null; then
            echo "${host_port} is available. Continuing..."
            break
        fi
    done
fi

# ensure the database is ready
while ! mysqladmin ping -h${MYSQL_HOST} -P${MYSQL_PORT} -u${MYSQL_USER} -p${MYSQL_PASSWORD} --silent; do
    echo "Waiting for mysql at ${MYSQL_HOST}:${MYSQL_PORT}..."
    sleep 1
done
echo "${MYSQL_HOST}:${MYSQL_PORT} is available. Continuing..."

# Create and migrate the database on first run
if ! mysql -h${MYSQL_HOST} -P${MYSQL_PORT} -u${MYSQL_USER} -p${MYSQL_PASSWORD} -e "desc ${MYSQL_DATABASE}.ACCESS" > /dev/null 2>&1; then
    echo "Creating database schema..."
    mysql -h${MYSQL_HOST} -P${MYSQL_PORT} -u${MYSQL_USER} -p${MYSQL_PASSWORD} ${MYSQL_DATABASE} < ${CREATE_DB_SQL}
fi

# prevent glob expansion in lib/*
set -f
LAUNCH_COMMAND='java -cp lib/* --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED i5.las2peer.tools.L2pNodeLauncher -s service -p '"${LAS2PEER_PORT} ${SERVICE_EXTRA_ARGS}"
if [[ ! -z "${BOOTSTRAP}" ]]; then
    LAUNCH_COMMAND="${LAUNCH_COMMAND} -b ${BOOTSTRAP}"
fi

# it's realistic for different nodes to use different accounts (i.e., to have
# different node operators). this function echos the N-th mnemonic if the
# variable WALLET is set to N. If not, first mnemonic is used
function selectMnemonic {
    declare -a mnemonics=("differ employ cook sport clinic wedding melody column pave stuff oak price" "memory wrist half aunt shrug elbow upper anxiety maximum valve finish stay" "alert sword real code safe divorce firm detect donate cupboard forward other" "pair stem change april else stage resource accident will divert voyage lawn" "lamp elbow happy never cake very weird mix episode either chimney episode" "cool pioneer toe kiwi decline receive stamp write boy border check retire" "obvious lady prize shrimp taste position abstract promote market wink silver proof" "tired office manage bird scheme gorilla siren food abandon mansion field caution" "resemble cattle regret priority hen six century hungry rice grape patch family" "access crazy can job volume utility dial position shaft stadium soccer seven")
    if [[ ${WALLET} =~ ^[0-9]+$ && ${WALLET} -lt ${#mnemonics[@]} ]]; then
    # get N-th mnemonic
        echo "${mnemonics[${WALLET}]}"
    else
        # note: zsh and others use 1-based indexing. this requires bash
        echo "${mnemonics[0]}"
    fi
}

#prepare pastry properties
echo external_address = $(curl -s https://ipinfo.io/ip):${LAS2PEER_PORT} > etc/pastry.properties

# start the service within a las2peer node
echo "now comes the moment of thruth"
if [[ -z "${@}" ]]
then
    if [ -n "$LAS2PEER_ETH_HOST" ]; then
        exec ${LAUNCH_COMMAND} --observer --ethereum-mnemonic "$(selectMnemonic)" uploadStartupDirectory startService\("'""${SERVICE}""'", "'""${SERVICE_PASSPHRASE}""'"\) startWebConnector "node=getNodeAsEthereumNode()" "registry=node.getRegistryClient()" "n=getNodeAsEthereumNode()" "r=n.getRegistryClient()" 
    else
        exec ${LAUNCH_COMMAND} --observer uploadStartupDirectory startService\("'""${SERVICE}""'", "'""${SERVICE_PASSPHRASE}""'"\) startWebConnector
    fi
else
  exec ${LAUNCH_COMMAND} ${@}
fi