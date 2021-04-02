import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { NotificationType } from '../enum/notifications-type.enum';
import { User } from '../model/user';
import { AuthenticationService } from '../service/authentication.service';
import { NotificationService } from '../service/notification.service';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent implements OnInit, OnDestroy {

  public showLoading: boolean;
  private subscriptions: Subscription[] = [];

  constructor(private router: Router, private authenticationService: AuthenticationService,
              private notificationService: NotificationService) { }

  ngOnInit(): void {
    if(this.authenticationService.isLoggedIn())
      this.router.navigateByUrl('/user/management');
  }

  public onRegister(user: User): void{
    this.showLoading = true;
    this.subscriptions.push(
      this.authenticationService.register(user).subscribe(
        //Primo argomento: cosa fare in caso di successo
        (response: User) =>{
          this.showLoading = false;
          this.sendNotification(NotificationType.SUCCESS, `A new account was created for ${response.firstName}.
                                           Please check your email for password to log in.`);
        },
        //Secondo argomento: cosa fare in caso di errore
        (errorResponse: HttpErrorResponse) => {
          this.sendNotification(NotificationType.ERROR, errorResponse.error.message);
          this.showLoading = false;
        }
      )
    );
  }

  sendNotification(notificationType: NotificationType, message: string) {
    if(message) 
      this.notificationService.notify(notificationType, message);
    else
      this.notificationService.notify(notificationType, 'AN ERROR OCCURRED, PLEASE TRY AGAIN');
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(sub => sub.unsubscribe());
    //come dire for(Subscription sub: subscriptions) { sub.unsuscribe }
  }
 
}
