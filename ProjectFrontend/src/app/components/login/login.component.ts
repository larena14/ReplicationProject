import {Component, ElementRef, NgZone, ViewChild} from '@angular/core';
import { CommonModule } from '@angular/common';
import {HttpService} from "../../services/http.service";
import {HttpErrorResponse} from "@angular/common/http";
import {FormControl, FormGroup, MinLengthValidator, NgForm, ReactiveFormsModule, Validators} from "@angular/forms";
import {MatInputModule} from "@angular/material/input";
import {Router} from "@angular/router";
import {MatDialog} from "@angular/material/dialog";
import {UploadDialogComponent} from "../upload-dialog/upload-dialog.component";
import {ForgetPasswordDialogComponent} from "../forget-password-dialog/forget-password-dialog.component";
import {MessageService} from "primeng/api";
import {ToastModule} from "primeng/toast";

@Component({
    selector: 'app-login',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, MatInputModule, ToastModule],
    templateUrl: './login.component.html',
    styleUrl: './login.component.css',
    providers: [MessageService]
})
export class LoginComponent {

    @ViewChild("username") username: ElementRef|null = null
    usernameControl = new FormControl(null, [Validators.required])
    @ViewChild("password") password: ElementRef|null = null
    passwordControl = new FormControl(null, [Validators.required, Validators.minLength(8), Validators.maxLength(16)])
    @ViewChild("form") form: NgForm|null = null
    formControl = new FormGroup([this.usernameControl, this.passwordControl])
    active: boolean = false
    unauthenticated: boolean = true
    errorMessage: string = "Incorrect username or password"

    @ViewChild("emailSignup") emailSignup: ElementRef|null = null
    emailSignupControl = new FormControl(null, [Validators.required, Validators.email])
    @ViewChild("usernameSignup") usernameSignup: ElementRef|null = null
    usernameSignupControl = new FormControl(null, [Validators.required])
    @ViewChild("passwordSignup") passwordSignup: ElementRef|null = null
    passwordSignupControl = new FormControl(null, [Validators.required, Validators.minLength(8), Validators.maxLength(16), Validators.pattern("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[-+_!@#$%^&*.,?]).+$")])
    @ViewChild("formSignup") formSignup: NgForm|null = null
    formSignupControl = new FormGroup([this.emailSignupControl, this.usernameSignupControl, this.passwordSignupControl])
    unregistered: boolean = true
    defaultErrorMessageSignup: string = "Something wrong, remember password must have: length between 8 and 16 characters, " +
        "at least one upper-case, one lower-case, one digit and one special character, no whitespaces"
    errorMessageSignup: string = this.defaultErrorMessageSignup



    constructor(private httpService: HttpService, private router: Router, private dialog: MatDialog,
                private ngZone: NgZone, private messageService: MessageService) {

    }
    login() {
        if(!this.form?.valid)
            return
        this.httpService.login(this.username?.nativeElement.value,
                        this.password?.nativeElement.value).subscribe(
            {
                next: value => {
                    console.log(value)
                    sessionStorage.setItem("accessToken", value.accessToken)
                    this.unauthenticated = false
                    this.router.navigate(['/dashboard/files'])
                },
                error: err => {
                    if(err.status == 0)
                        this.errorMessage = "Unable to Connect to the Server";
                    this.unauthenticated = true
                }
            })
    }

    signup(){
        if(!this.formSignup?.valid)
            return
        this.httpService.signUp(this.emailSignup?.nativeElement.value,
            this.passwordSignup?.nativeElement.value,
            this.usernameSignup?.nativeElement.value).subscribe(
            {
                next: value => {
                    console.log(value)
                    this.unregistered = false
                    this.messageService.add({
                        severity: "success",
                        summary: "Success",
                        detail: value+" Please login."
                    });
                },
                error: err => {
                    if(err.status == 0)
                        this.errorMessageSignup = "Unable to Connect to the Server";
                    this.unregistered = true
                }
            })
    }

    switch(){
        this.active = !this.active;
        this.unauthenticated = false;
        this.errorMessage = "Incorrect username or password";
        this.unregistered = false;
        this.errorMessageSignup = this.defaultErrorMessageSignup;

    }

    input(){
        this.unauthenticated = false;
        this.unregistered = false;
        this.errorMessage = "Incorrect username or password";
        this.errorMessageSignup = this.defaultErrorMessageSignup;
    }

    getErrorMessage(fieldControl: FormControl<null>) {
        return fieldControl.hasError('required') ? 'You must enter a value' :
            fieldControl.hasError('minlength') ? 'Minimum length is 8 characters' :
                fieldControl.hasError('maxlength') ? 'Maximum length is 16 characters' :
                    fieldControl.hasError('email') ? 'Email not valid' :
                        fieldControl.hasError('pattern') ? this.defaultErrorMessageSignup :
                '';
    }

    getRequestErrorMessage(){
        return this.errorMessage;
    }

    getRequestSignupErrorMessage(){
        return this.errorMessageSignup;
    }

    forgetPassword(){
        this.ngZone.run(() => {
            this.dialog.open(ForgetPasswordDialogComponent);
        });
    }

}


