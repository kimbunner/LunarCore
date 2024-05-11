package emu.lunarcore.server.http.handlers;

import org.jetbrains.annotations.NotNull;

import emu.lunarcore.LunarCore;
import emu.lunarcore.game.account.Account;
import emu.lunarcore.game.account.AccountHelper;
import emu.lunarcore.server.http.objects.LoginAccountReqJson;
import emu.lunarcore.server.http.objects.LoginResJson;
import emu.lunarcore.server.http.objects.LoginResJson.VerifyData;
import emu.lunarcore.util.JsonUtils;
import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.javalin.http.Handler;

public class UsernameLoginHandler implements Handler {

    public UsernameLoginHandler() {

    }

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        // Setup response
        LoginResJson res = new LoginResJson();

        // Parse request
        LoginAccountReqJson req = JsonUtils.decode(ctx.body(), LoginAccountReqJson.class);

        // Validate
        if (req == null) {
            res.retcode = -202;
            res.message = "Error logging in";
            return;
        }

        var reget = req.account.split(";");

        if (reget.length != 2){
            res.retcode = -201;
            res.message = "Check what you type";
        }

        // Login - Get account data
        Account account = LunarCore.getAccountDatabase().getObjectByField(Account.class, "username", reget[0]);

        if (account == null) {
            // Auto create an account for the player if allowed in the config
            if (LunarCore.getConfig().getServerOptions().autoCreateAccount) {
                account = AccountHelper.createAccount(reget[0], reget[1], 0);
                res.retcode = -201;
                res.message = "Username: "+reget[0]+" Password: "+reget[1];
            } else {
                res.retcode = -201;
                res.message = "Username not found.";
            }
        } 
        
        if (account != null) {
            if (reget[1].toString().contentEquals(account.getPassword())){
                res.message = "OK";
                res.data = new VerifyData(account.getUid(), account.getEmail(), account.getPassword(), account.generateDispatchToken());
            } else {
                res.retcode = -201;
                res.message = "Wrong password.";
            }
        }

        // Send result
        ctx.contentType(ContentType.APPLICATION_JSON);
        ctx.result(JsonUtils.encode(res));
    }

}
