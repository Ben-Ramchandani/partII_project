max = 25

base = 32

f = File.open("rbdataoutrsa.txt", "w")

for i in 1..max
    size = base * 2**i
    puts "Generating #{size} byte file. (#{size/(2**20)}MB)"
    `dd if=/dev/urandom of=randruby.dat  bs=#{size}  count=1 2> /dev/null`
    puts "Starting timed run."
    start = Time.now()
    `java -jar filepay.jar -p 0xfe88c94d860f01a17f961bf4bdfb6e0c6cd10d3fda5cc861e805ca1240c58553 randruby.dat -c -m 1 -r`
    endt = Time.now()
    puts "Time was #{endt-start}s."
    f.write("#{endt-start}\n")
end