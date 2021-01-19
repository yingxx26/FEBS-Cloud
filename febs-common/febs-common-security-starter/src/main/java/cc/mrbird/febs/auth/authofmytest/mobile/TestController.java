package cc.mrbird.febs.auth.authofmytest.mobile;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * 生成校验码的请求处理器
 *
 * @Author yxx
 * @Date 2021/1/19 18:15
 */
@Slf4j
@RestController
public class TestController {

    /**
     * 创建验证码，根据验证码类型不同，调用不同的 {@link  }接口实现
     *
     * @param request  the request
     * @param response the response
     * @throws Exception the exception
     */
    @PostMapping("/test/code/sms")
    public void createCode(HttpServletRequest request, HttpServletResponse response) throws Exception {
        //redisService.set("mytest", "123456");
        System.out.println("send mytest===================== 123456");
    }

    /**
     * Check code object.
     *
     * @param request  the request
     * @param response the response
     * @return the object
     */
    @GetMapping("/test/code/1")
    public Object checkCode(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("checkCode mytest===================== 123456");
        return null;
    }
}
