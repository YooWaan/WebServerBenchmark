#!/bin/sh

function writeTestTimeToFile() {
	bench_server=$1

	if [ "${bench_server}" = "" ]; then
		bench_server="apache"
	fi

	file_prefix="result-${bench_server}"
	result_dir="result/${bench_server}"

	for request_count in 200 500 1500 10000 30000  ; do
#	for request_count in 10000 30000  ; do
		row_line="${request_count}"
		avrg_num=0
#		for test_time in `awk '{ if (/Time taken/) { printf("%s\n", $5) } else if (/Failed/) { printf("%s\n", $3)} else if (/Requests/) {printf("%s\n",$4)} }' ${result_dir}/${file_prefix}-${request_count}-*.txt `; do
		for test_time in `grep 'Time taken' ${result_dir}/${file_prefix}-${request_count}-*.txt | awk '{printf("%s\n", $5)}' `; do
			row_line="${row_line}\t${test_time}"
#			avrg_num=`echo "sacle=7; ${avrg_num} + ${test_time}" | bc`
		done
#		avrg_num=`echo "sacle=7; ${avrg_num} / 4" | bc`
#		row_line="${row_line}\t${avrg_num}"

		echo $row_line
	done
}


rm result/*-time.dat
for srv in `ls result/` ; do
	writeTestTimeToFile $srv > result/${srv}-time.dat
done

for f in `ls result/*-time.dat`; do
	echo $f;
	printf "%s\t%s\t%s\t%s\t%s\n" request 1 2 3 4
#	printf "%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n" request 1 failed "req/sec" 2 failed "req/sec" 3 failed "req/sec" 4 failed "req/sec"
	cat $f;
done


exit

plot_option="using 1:2:3:4 with yerrorbars title"
plot_bar_option="using 1:6 with line title"

gnuplot <<EOF
set terminal png font "/Library/Fonts/Arial.ttf" 14
set output "test_time.png"

set key left top
set xlabel 'Request'
set ylabel 'Test time (second)'

set xtics ("10000" 10000, "30000" 30000)
set xrange [9000:31000]
set boxwidth 1000

plot \
"result/apache-time.dat" ${plot_bar_option} "Apache",\
"result/apache-time.dat" ${plot_option} "Apache",\
"result/lighttpd-time.dat" ${plot_bar_option} "Lighttpd",\
"result/lighttpd-time.dat" ${plot_option} "Lighttpd",\
"result/nodejs-time.dat" ${plot_bar_option} "Nodejs",\
"result/nodejs-time.dat" ${plot_option} "Nodejs",\
"result/warp-time.dat" ${plot_bar_option} "Warp",\
"result/warp-time.dat" ${plot_option} "Warp",\
"result/tomcat-time.dat" ${plot_bar_option} "Tomcat",\
"result/tomcat-time.dat" ${plot_option} "Tomcat",\
"result/jetty-time.dat" ${plot_bar_option} "Jetty",\
"result/jetty-time.dat" ${plot_option} "Jetty"

exit
EOF


# E O F