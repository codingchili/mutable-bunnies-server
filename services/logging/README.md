# logging service

Receives logging data from the services deployed in the cluster and authorized clients.

#### Feature set

- [x] output to ElasticSearch
- [x] output to Console
- [x] upload data using HTTP/REST to ES
- [x] handle system & client logging contexts


PUT /test
{
"settings": {
"number_of_shards": 1
},
"mappings": {
"properties": {
"field1": { "type": "text" }
}
}
}

to run in with the ELK backend in docker,
docker run --name elasticsearch -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" docker.elastic.co/elasticsearch/elasticsearch:7.11.1
docker run --link elasticsearch:elasticsearch -p 5601:5601 docker.elastic.co/kibana/kibana:7.11.1