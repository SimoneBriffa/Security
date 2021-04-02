import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { NotificationType } from '../enum/notifications-type.enum';
import { AuthenticationService } from '../service/authentication.service';
import { NotificationService } from '../service/notification.service';

/*Per creare questa classe bisogna chiamare ng g guard /guard/authentication --skipTests;
skipTests non è importante, evita solo di creare il file .js di test, appunto. 

Questa è la classe per la restrizione degli accessi */

@Injectable({providedIn: 'root'})
export class AuthenticationGuard implements CanActivate {

  constructor(private authenticationService: AuthenticationService, 
              private router: Router, 
              private notificationService: NotificationService){}

  canActivate(next: ActivatedRouteSnapshot,
              state: RouterStateSnapshot): boolean {
    return this.isUserLoggedIn();
  }
  
  private isUserLoggedIn(): boolean {
      if(this.authenticationService.isLoggedIn())
        return true;
      else{
        this.router.navigate(['/login']);
        this.notificationService.notify(NotificationType.ERROR, 
                `You need to log in to access this page`.toUpperCase());
        return false;
      }
  }

}
