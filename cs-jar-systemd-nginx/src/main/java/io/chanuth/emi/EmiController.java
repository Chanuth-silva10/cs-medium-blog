package io.chanuth.emi;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class EmiController {

    private final EmiCalculator calculator;

    public EmiController(EmiCalculator calculator) {
        this.calculator = calculator;
    }

    @GetMapping("/")
    public String form(Model model) {
        model.addAttribute("loanForm", new LoanForm());
        return "index";
    }

    @PostMapping("/")
    public String calculate(@Valid @ModelAttribute("loanForm") LoanForm form, BindingResult errors, Model model) {
        if (!errors.hasErrors()) {
            model.addAttribute("result", calculator.calculate(form.getAmount(), form.getRate(), form.getMonths()));
        }
        return "index";
    }

    /**
     * Shows what the application believes about the request. Behind a reverse proxy it is the way to
     * check that the scheme, the host and the client address survive the trip.
     */
    @GetMapping("/whoami")
    @ResponseBody
    public Map<String, String> whoAmI(HttpServletRequest request) {
        return Map.of(
                "scheme", request.getScheme(),
                "host", request.getServerName(),
                "clientIp", request.getRemoteAddr());
    }
}

