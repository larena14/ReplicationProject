import {Component, ElementRef, ViewChild} from '@angular/core';
import { CommonModule } from '@angular/common';
import {FormControl, FormGroup, FormsModule, NgForm, ReactiveFormsModule, Validators} from "@angular/forms";
import {MatFormFieldModule} from "@angular/material/form-field";
import {HttpService} from "../../services/http.service";
import {Router} from "@angular/router";
import {ToastModule} from "primeng/toast";
import {MessageService} from "primeng/api";

@Component({
  selector: 'app-change-password',
  standalone: true,
    imports: [CommonModule, FormsModule, MatFormFieldModule, ReactiveFormsModule, ToastModule],
  templateUrl: './change-password.component.html',
  styleUrl: './change-password.component.css',
    providers: [MessageService]
})
export class ChangePasswordComponent {
    @ViewChild("previousPassword") previousPassword: ElementRef|null = null
    previousPasswordControl = new FormControl(null, [Validators.required, Validators.minLength(8), Validators.maxLength(16), Validators.pattern("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[-+_!@#$%^&*.,?]).+$")])
    @ViewChild("password") password: ElementRef|null = null
    passwordControl = new FormControl(null, [Validators.required, Validators.minLength(8), Validators.maxLength(16), Validators.pattern("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[-+_!@#$%^&*.,?]).+$")])
    @ViewChild("passwordConfirm") passwordConfirm: ElementRef|null = null
    passwordConfirmControl = new FormControl(null, [Validators.required, Validators.minLength(8), Validators.maxLength(16), Validators.pattern("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[-+_!@#$%^&*.,?]).+$")])
    @ViewChild("form") form: NgForm|null = null
    formControl = new FormGroup([this.previousPasswordControl, this.passwordControl, this.passwordConfirmControl])
    unregistered: boolean = true
    defaultErrorMessage: string = "Something wrong, remember password must have: length between 8 and 16 characters, " +
        "at least one upper-case, one lower-case, one digit and one special character, no whitespaces"
    errorMessage: string = this.defaultErrorMessage

    constructor(private httpService: HttpService, private router: Router,  private messageService: MessageService) {

    }

    changePassword(){
        if(!this.form?.valid || this.passwordControl.value !== this.passwordConfirmControl.value){
            this.unregistered = true;
            this.errorMessage = this.defaultErrorMessage+". Passwords must be equals!";
            return;
        }
        this.httpService.changePassword(
            this.previousPassword?.nativeElement.value,
            this.password?.nativeElement.value,
            this.passwordConfirm?.nativeElement.value
        ).subscribe(
            {
                next: value => {
                    this.messageService.add({
                        severity: "success",
                        summary: "Success",
                        detail: "The password has been changed."
                    });
                    this.router.navigate(['/dashboard/user']);
                },
                error: err => {
                    if(err.status == 0)
                        this.errorMessage = "Unable to Connect to the Server";
                    this.unregistered = true;
                    this.errorMessage = this.defaultErrorMessage+". Passwords must be equals and the previous password must be correct!";
                }
            }
        )
    }

    input(){
        this.unregistered = false;
        this.errorMessage = this.defaultErrorMessage;
    }

    getErrorMessage(fieldControl: FormControl<null>) {
        return fieldControl.hasError('required') ? 'You must enter a value' :
            fieldControl.hasError('minlength') ? 'Minimum length is 8 characters' :
                fieldControl.hasError('maxlength') ? 'Maximum length is 16 characters' :
                    fieldControl.hasError('email') ? 'Email not valid' :
                        fieldControl.hasError('pattern') ? this.defaultErrorMessage :
                        '';
    }

    getRequestErrorMessage(){
        return this.errorMessage;
    }

}
