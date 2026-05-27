import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-negotiation-manager-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './negotiation-manager-layout.component.html',
  styleUrls: ['./negotiation-manager-layout.component.css']
})
export class NegotiationManagerLayoutComponent {
  currentManagerName = 'David Toth'; // Kasnije se treba povlaciti iz JWT
}