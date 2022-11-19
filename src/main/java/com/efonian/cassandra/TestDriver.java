package com.efonian.cassandra;

import com.efonian.cassandra.util.UtilMathLong;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TestDriver {
    public static void main(String[] args) {
        try {
            isPrimeTest();
            primeFactorizationPollardRhoTest();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void isPrimeTest() throws IOException {
        List<Long> primes = Files.lines(Path.of("./src/main/resources/lookup/prime10000.txt"))
                .map(l -> l.trim().split("\\s+"))
                .flatMap(Arrays::stream)
                .map(Long::parseLong)
                .collect(Collectors.toList());
        
        for(long i = 0; i < 100000; i++) {
            boolean result = UtilMathLong.isPrime(i);
            if(result != primes.contains(i))
                System.out.println("Failed: i = " + i);
        }
    }
    
    public static void primeFactorizationPollardRhoTest() {
        for(long i = 0; i < 10000; i++) {
            List<Long> factors = UtilMathLong.primeFactorizationPollardRho(i);
            System.out.println("i = " + i + ": " + factors);
        }
    }
}
