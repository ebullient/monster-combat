source ./.env
DOCKER_CONTENT_TRUST=1 \
docker run -d -v /var/run/docker.sock:/var/run/docker.sock:ro \
              -v /proc/:/host/proc/:ro \
              -v /sys/fs/cgroup/:/host/sys/fs/cgroup:ro \
              -e DD_API_KEY="${DATADOG_API_KEY}" \
              -e DD_DOGSTATSD_NON_LOCAL_TRAFFIC="true" \
              -p 8125:8125/udp \
              datadog/dogstatsd
