export interface AuthenticationResponseModel{
    username: string
    accessToken: string
    idToken: string
    refreshToken: string
    sessionId: string
    challengeType: string
    message: string
}
