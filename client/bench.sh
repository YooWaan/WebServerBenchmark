#!/bin/sh


function bench() {
	prefix_of_file=$1
	request=$2
	connect_url=$3
	for num in $(seq 1 4); do
		gnuplot_file="${prefix_of_file}-${request}-${num}.dat"
		result_file="${prefix_of_file}-${request}-${num}.txt"

#		echo "url    : ${connect_url} , request : ${request}"
		echo "request : ${request} , result : ${result_file} , data    : ${gnuplot_file}"
#		cmd="ab -r -n ${request} -c 128 -g ${gnuplot_file} ${connect_url} > ${result_file}"
#		echo $cmd
#		$cmd

		ab -r -n ${request} -c 128 -g ${gnuplot_file} ${connect_url} > ${result_file}
	done


	# make result png
	gnuplot <<EOF
set terminal png font "/Library/Fonts/Arial.ttf" 14
set output "${bench_server}_${request}.png"

set key left top
set xlabel 'Request ( ${bench_server} )'
set ylabel 'wait time (ms)'

plot "${prefix_of_file}-${request}-1.dat" using 10 with points title "n=1" , "${prefix_of_file}-${request}-2.dat" using 10 with points title "n=2" , "${prefix_of_file}-${request}-3.dat" using 10 with points title "n=3" , "${prefix_of_file}-${request}-4.dat" using 10 with points title "n=4"


exit
EOF

}


bench_server=$1
url="http://192.168.5.160"

if [ ${bench_server} = "jetty" ]; then
url="${url}:8080/sample/index.html"
elif [ ${bench_server} = "tomcat" ]; then
url="${url}:8080/sample/index.html"
elif [ ${bench_server} = "warp" ]; then
url="${url}:8123/index"
elif [ ${bench_server} = "nodejs" ]; then
url="${url}:8124/"
elif [ ${bench_server} = "lighttpd" ]; then
url="${url}:8123/index.html"
else
bench_server="apache"
#url="${url}/index.html"
url="${url}/sample.html"
fi

file_prefix="result-${bench_server}"
result_dir="result/${bench_server}"

if [ ! -d result ]; then
	mkdir result
fi

if [ -d ${result_dir} ] ; then
	rm -rf $result_dir
fi

mkdir $result_dir

echo "connect : ${url} , ${result_dir}"

for request_count in 30000 10000 1500 500 200 ; do
#for request_count in 200 ; do

bench "${result_dir}/${file_prefix}" $request_count $url

done


# E O F
