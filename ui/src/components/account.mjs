
export async function request(endpoint, body = null) {
    try {
        return await Caffeinated.auth.sendAuthorizedApiRequest(endpoint, body);
    } catch (e) {
        if (e.includes("CasterlabsApiException")) {
            throw JSON.parse(e.split("CasterlabsApiException: ")[1].split("\n")[0]);
        }
    }
}

export function logout() {
    Caffeinated.auth.logoutCasterlabs();
}

export async function login(email, password) {
    const response = await request("/v3/auth/login", {
        email,
        password,
    });

    Caffeinated.auth.loginCasterlabs(response.token);

    return await request("/v3/account");
}

export async function register(name, email, password) {
    const response = await request("/v3/auth/register", {
        name,
        email,
        password,
    });

    Caffeinated.auth.loginCasterlabs(response.token);

    return response.account;
}

export async function requestPasswordReset(email) {
    const response = await request("/v3/account/requestpasswordreset", {
        email
    });

    console.debug("success:", response.success);
}

export async function sendEmailVerification() {
    await request("/v3/account/sendemailverification", {});
}

export async function verifyEmail(id) {
    await request("/v3/account/verifyemail", {
        id
    });
}

export async function resetPassword(id, newPassword) {
    await request("/v3/account/resetpassword", {
        id,
        newPassword
    });
}

export async function getMyAccount() {
    return await Caffeinated.auth.casterlabsAccount;
}
