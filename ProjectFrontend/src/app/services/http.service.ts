import {Injectable} from "@angular/core";
import {HttpClient, HttpHeaders, HttpParams} from "@angular/common/http";
import {UserSignUpRequestModel} from "../models/user-sign-up-request.model";
import {LoginRequestModel} from "../models/login-request.model";
import {ChangeUserPasswordRequestModel} from "../models/change-user-password-request.model";
import {AuthenticationResponseModel} from "../models/authentication-response.model";
import {TagIdModel} from "../models/tag-id.model";
import {PutObjectRequestModel} from "../models/put-object-request.model";


@Injectable({
    providedIn: 'root'
})
export class HttpService{

    private baseUrl = "http://***:8080/"; // insert IP
    private authApi = {
        signup: this.baseUrl+"auth/sign-up",
        login: this.baseUrl+"auth/login",
        changePassword: this.baseUrl+"auth/change-password",
        logout: this.baseUrl+"auth/logout",
        forgotPassword: this.baseUrl+"auth/forgot-password",
    }
    private nodesApi = {
        testAll: this.baseUrl+"nodes/test/all",
        test: this.baseUrl+"nodes/test",
        listObjects: this.baseUrl+"nodes/objects/all",
        searchObjects: this.baseUrl+"nodes/objects/search",
        putObject: this.baseUrl+"nodes/objects",
        getObjectsURL: this.baseUrl+"nodes/objects/url",
        getObject: this.baseUrl+"nodes/objects",
        deleteObject: this.baseUrl+"nodes/objects"
    }

    constructor(private httpClient: HttpClient) {}

    headers(){
        const token = sessionStorage.getItem("accessToken")
        if(token === null)
            throw new Error("Token is null!")
        return new HttpHeaders().set("Authorization", "Bearer "+token);
    }


    signUp(email: string, password: string, username: string){
        let bodyRequest: UserSignUpRequestModel = {
            email: email,
            password: password,
            username: username,
            roles: ["USER"]
        }
        return this.httpClient.post(this.authApi.signup, bodyRequest, {responseType: "text"});
    }

    login(username: string, password: string){
        let bodyRequest: LoginRequestModel = {
            username: username,
            password: password
        }
        return this.httpClient.post<AuthenticationResponseModel>(this.authApi.login, bodyRequest);
    }

    changePassword(previousPassword: string, password: string, passwordConfirm: string){
        let bodyRequest: ChangeUserPasswordRequestModel = {
            previousPassword: previousPassword,
            password: password,
            passwordConfirm: passwordConfirm
        }
        return this.httpClient.put(this.authApi.changePassword, bodyRequest, {responseType: "text", headers: this.headers()});
    }

    forgetPassword(email: string){
        return this.httpClient.get(this.authApi.forgotPassword, {params: new HttpParams().set('email', encodeURI(email)), responseType: "text"});
    }

    logout(){
        return this.httpClient.delete(this.authApi.logout, {responseType: "text", headers: this.headers()});
    }

    getNodes(){
        return this.httpClient.get<TagIdModel[]>(this.nodesApi.testAll, {headers: this.headers()})
    }

    testNode(instanceId: string){
        return this.httpClient.get(this.nodesApi.test, {params: new HttpParams().set('instanceId', encodeURI(instanceId)), responseType: "text", headers: this.headers()});
    }

    listObjects(instanceId: string){
        return this.httpClient.get<string[]>(this.nodesApi.listObjects, {params: new HttpParams().set('instanceId', encodeURI(instanceId)), headers: this.headers()});
    }

    searchObjects(instanceId: string, prefix: string){
        return this.httpClient.get<string[]>(this.nodesApi.searchObjects, {headers: this.headers(), params: new HttpParams().set('prefix', encodeURI(prefix)).set('instanceId', encodeURI(instanceId))});
    }

    putObject(instanceId: string, key: string, fileBytes: any){
        let bodyRequest: PutObjectRequestModel = {
            instanceId: instanceId,
            key: key,
            fileBytes: fileBytes
        }
        return this.httpClient.post(this.nodesApi.putObject, bodyRequest, {headers: this.headers(), responseType: "text"});
    }

    getObjectURL(instanceId: string, key: string){
        return this.httpClient.get(this.nodesApi.getObjectsURL, {headers: this.headers(), responseType: "text", params: new HttpParams().set('instanceId', encodeURI(instanceId)).set('key', encodeURI(key))});
    }

    getObject(instanceId: string, key: string){
        return this.httpClient.get(this.nodesApi.getObject, {headers: this.headers(), responseType: "blob", params: new HttpParams().set('instanceId', encodeURI(instanceId)).set('key', encodeURI(key))});
    }

    deleteObject(instanceId: string, key: string){
        return this.httpClient.delete(this.nodesApi.deleteObject, {headers: this.headers(), responseType: "text", params: new HttpParams().set('instanceId', encodeURI(instanceId)).set('key', encodeURI(key))});
    }




}

