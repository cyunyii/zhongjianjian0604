package cn.xmu.files;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
@SpringBootApplication(scanBasePackages = {"cn.xmu","cn.xmu.files"})
@MapperScan("cn.xmu.files.mapper")
public class Application {
    
    public static void main(String[] args){
        SpringApplication.run(Application.class, args);
    }
}
