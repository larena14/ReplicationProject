import {Component, OnInit} from '@angular/core';
import { CommonModule } from '@angular/common';
import {Router} from "@angular/router";
import { JwtHelperService } from '@auth0/angular-jwt';

@Component({
  selector: 'app-user',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './user.component.html',
  styleUrl: './user.component.css'
})
export class UserComponent implements OnInit{

    username: string = '';
    helper: JwtHelperService = new JwtHelperService();

    constructor(private router: Router) {

    }

    ngOnInit(): void {
        const token = sessionStorage.getItem("accessToken");
        if(token === null)
            throw new Error("Token is null!");
        this.username = this.helper.decodeToken(token).username;
    }

    changePassword(){
        this.router.navigate(['/dashboard/change-password']);
    }
}
