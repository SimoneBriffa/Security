import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor
} from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthenticationService } from '../service/authentication.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  //N.B. per creare questa classe bisogna chiamare ng g interceptor <nome>

  //Questa è la classe che intercetta il Token

  constructor(private authenticationService: AuthenticationService) {}

  intercept(httpRequest: HttpRequest<any>, httpHandler: HttpHandler): Observable<HttpEvent<any>> {
    if(httpRequest.url.includes(`${this.authenticationService.host}/user/login`))
      return httpHandler.handle(httpRequest);

      if(httpRequest.url.includes(`${this.authenticationService.host}/user/register`))
        return httpHandler.handle(httpRequest);

      if(httpRequest.url.includes(`${this.authenticationService.host}/user/resetPassword`))
        return httpHandler.handle(httpRequest);

      //cioè se siamo in login, register o resetPassword non fare niente di particolare

      this.authenticationService.loadToken();
      //carica il token
      const token = this.authenticationService.getToken();
      //assegnalo a una variabile
      const request = httpRequest.clone({setHeaders: { Authorization: `Bearer ${token}`}});
      //poichè la richiesta è immutabile, prima di passarla, cloanala
      return httpHandler.handle(request);
  }



}
