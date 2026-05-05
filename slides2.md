---
theme: .theme.json
---
**Fussball**
---
Det er gøy å spille. Enda gøyere å vinne.
---
Men hva skjer etter en seier?
---
Joda, du får ære og skryt og alle liker deg
---
Men hva skjer når støvet har lagt seg?
---
Når middagen er spist, nattesøvnen over, og 4 etasje i Nøstegaten 58 på ny fylles opp med kaffe, databrus, og gode folk -
---
Kommer noen til å huske gårdagens triumf?
---
Nei.
---
Men det var før.
---
Nå?
---
Nå holder Aki tellinga.
---
Dine fussballopp- og nedturer noteres og lagres
---
Men hva om Aki skal på rutinesjekk hos tannlegen etter lunsj?
---
Da trenger man kanskje en app
---

# Fussball app
- Lagre fussballresultater
- Styrke kompetanse
  - koding
  - og fussball
- Nysgjerrig på self hosting

---

# Stack

Team Pingvin relevanse

## Backend
- Kotlin
- Spring
- Maven
- Postgres

## Frontend
- Angular
- Tailwind
- TypeScript

---

# Introduksjon

```
~~~graph-easy --as=boxart

[ Frontenden ] -> [ Signal forms ]
~~~
```

Frontenden er i kjernen et signal form.


---
# Signal Form


```
~~~graph-easy --as=boxart
[ Model ] - brukes i -> [ form() ] - lager et -> [ Signal form ]
~~~
```

---
# Signal Form


```
~~~graph-easy --as=boxart
[ Model ] - brukes i -> [ form() ] - lager et -> [ Signal form ]
~~~
```

## Eksempel
```typescript
// Lag en datastruktur
interface LoginData {
  email: string;
  password: string;
}
```
---
# Signal Form


```
~~~graph-easy --as=boxart
[ Model ] - brukes i -> [ form() ] - lager et -> [ Signal form ]
~~~
```

## Eksempel
```typescript
// Lag en datastruktur
interface LoginData {
  email: string;
  password: string;
}

// Lag en model med datastrukturen
loginModel = signal<LoginData>({
  email: '',
  password: '',
});

```
---
# Signal Form

```
~~~graph-easy --as=boxart
[ Model ] - brukes i -> [ form() ] - lager et -> [ Signal form ]
~~~
```

## Eksempel
```typescript
// Lag en datastruktur
interface LoginData {
  email: string;
  password: string;
}

// Lag en model med datastrukturen
loginModel = signal<LoginData>({
  email: '',
  password: '',
});

// Send modelen til form()
loginForm = form(loginModel);

loginForm // Signal form
```

---
Men hva ER et signal form?
---

# Hva er et signal form?

```
~~~graph-easy --as=boxart
[ Model ] - brukes i -> [ form() ] - lager et -> [ Signal form ]
~~~
```

## FieldTree

## FieldState

## [formField]
---

# Hva er et signal form?
```
~~~graph-easy --as=boxart
[ Model ] - brukes i -> [ form() ] - lager et -> [ Signal form ]
~~~
```

## FieldTree

1. `form(Model)` lager et signal form


## FieldState

## [formField]
---

# Hva er et signal form?


```
~~~graph-easy --as=boxart
[ Model ] - brukes i -> [ form() ] - lager et -> [ FieldTree ]
~~~
```

## FieldTree

1. `form(Model)` lager et ~~signal form~~ `FieldTree`

## FieldState

## [formField]
---

# Hva er et signal form?

```
~~~graph-easy --as=boxart
[ Model ] - brukes i -> [ form() ] - lager et -> [ FieldTree ]
~~~
```

## FieldTree
1. `form(Model)` lager et ~~signal form~~ `FieldTree`
1. Strukturen til et `FieldTree` speiler `Model` - helt eller delvis

## FieldState

## [formField]
---

# Hva er et signal form?

```
~~~graph-easy --as=boxart
[ Model ] - brukes i -> [ form() ] - lager et -> [ FieldTree ]
~~~
```

## FieldTree
1. `form(Model)` lager et ~~signal form~~ `FieldTree`
1. Strukturen til et `FieldTree` speiler `Model` - helt eller delvis

### Eksempel
```typescript
interface Match {
  team1: {
    players: {
      offense: string;
      defense: string;
    }
    score: number;
  },
  ...
}

matchForm = form(signal<Match>(...)); // FieldTree

// Har alle feltene til Match
matchForm
matchForm.team1
matchForm.team1.players
matchForm.team1.players.offense
matchForm.team1.players.defense
matchForm.team1.score
...
```

## FieldState

## [formField]
---

# Hva er et signal form?

```
~~~graph-easy --as=boxart
[ Model ] - brukes i -> [ form() ] - lager et -> [ FieldTree ]
~~~
```

## FieldTree
1. `form(Model)` lager et ~~signal form~~ `FieldTree`
1. Strukturen til et `FieldTree` speiler `Model` - helt eller delvis
1. Et felt i et `FieldTree` er også et `FieldTree`

## FieldState

## [formField]
---

# Hva er et signal form?

```
~~~graph-easy --as=boxart
[ Model ] - brukes i -> [ form() ] - lager et -> [ FieldTree ]
~~~
```

## FieldTree
1. `form(Model)` lager et ~~signal form~~ `FieldTree`
1. Strukturen til et `FieldTree` speiler `Model` - helt eller delvis
1. Et felt i et `FieldTree` er også et `FieldTree`

### Eksempel

```typescript
matchForm                          // FieldTree<Match>
matchForm.team1                    // FieldTree<Team>
matchForm.team1.players            // FieldTree<Players>
matchForm.team1.players.offense    // FieldTree<string>
matchForm.team1.players.defense    // FieldTree<string>
matchForm.team1.score              // FieldTree<number>
...
```

## FieldState

## [formField]
---

# Hva er et signal form?

```
~~~graph-easy --as=boxart
[ Model ] - brukes i -> [ form() ]
[ form() ] - lager et -> [ FieldTree ]
~~~
```
## FieldTree
1. `form(Model)` lager et ~~signal form~~ `FieldTree`
1. Strukturen til et `FieldTree` speiler `Model` - helt eller delvis
1. Et felt i et `FieldTree` er også et `FieldTree`

## FieldState

## [formField]
---

# Hva er et signal form?

```
~~~graph-easy --as=boxart
[ Model ] - brukes i -> [ form() ]
[ form() ] - lager et -> [ FieldTree ]
[ FieldTree ] - har felter som er -> [ FieldTree]
~~~
```
## FieldTree

## FieldState

## [formField]
---
# Hva er et signal form?

```
~~~graph-easy --as=boxart
[ Model ] - brukes i -> [ form() ]
[ form() ] - lager et -> [ FieldTree ]
[ FieldTree ] - har felter som er -> [ FieldTree ]
~~~
```
## FieldTree

## FieldState
1. Lar oss lese eller skrive verdier til et `FieldTree`

## [formField]
---

# Hva er et signal form?

```
~~~graph-easy --as=boxart
[ Model ] - brukes i -> [ form() ]
[ form() ] - lager et -> [ FieldTree ]
[ FieldTree ] - har felter som er -> [ FieldTree ]
~~~
```
## FieldTree

## FieldState
1. Lar oss lese eller skrive verdier til et `FieldTree`
1. Inneholder all `state` knyttet til et `FieldTree`
    - value
    - touched
    - dirty
    - hidden
    - errors
    - mer...

## [formField]

---

# Hva er et signal form?

```
~~~graph-easy --as=boxart
[ Model ] - brukes i -> [ form() ]
[ form() ] - lager et -> [ FieldTree ]
[ FieldTree ] - har felter som er -> [ FieldTree ]
~~~
```
## FieldTree

## FieldState
1. Lar oss lese eller skrive verdier til et `FieldTree`
1. Inneholder all `state` knyttet til et `FieldTree`
    - value
    - touched
    - dirty
    - hidden
    - errors
    - mer...

  
### Eksempel

```typescript
// FieldTree
matchForm                             // FieldTree<Match>
matchForm.team1                       // FieldTree<Team>
matchForm.team1.players               // FieldTree<Players>
matchForm.team1.players.offense       // FieldTree<string>
matchForm.team1.players.defense       // FieldTree<string>
matchForm.team1.score                 // FieldTree<number>

// FieldState
matchForm()                           // FieldState
matchForm.team1()                     // FieldState
matchForm.team1().value()             // { players: { offense: "", defense: ""}}
matchForm().value().team1.players     // { offense: "", defense: ""}

// value.set
matchForm.team1.players.offense().value.set("Aki")
```

## [formField]
---

# Hva er et signal form?

```
~~~graph-easy --as=boxart
[ Model ] - brukes i -> [ form() ]
[ form() ] - lager et -> [ FieldTree ]
[ FieldTree ] - har felter som er -> [ FieldTree ]
~~~
```
## FieldTree

## FieldState
1. Lar oss lese eller skrive verdier til et `FieldTree`
1. Inneholder all `state` knyttet til et `FieldTree`
    - value
    - touched
    - dirty
    - hidden
    - errors
    - mer...

  ## [formField]
---

# Hva er et signal form?

```
~~~graph-easy --as=boxart
[ Model ] - brukes i -> [ form() ]
[ form() ] - lager et -> [ FieldTree ]
[ FieldTree ] - har felter som er -> [ FieldTree ]
[ FieldState ] - leser og skriver -> [ FieldTree ]
~~~
```
## FieldTree

## FieldState

## [formField]
---

# Hva er et signal form?

```
~~~graph-easy --as=boxart
[ Model ] - brukes i -> [ form() ]
[ form() ] - lager et -> [ FieldTree ]
[ FieldTree ] - har felter som er -> [ FieldTree ]
[ FieldState ] - leser og skriver -> [ FieldTree ]
~~~
```
## FieldTree

## FieldState

## [formField]
1. binder `FieldState` og `HTML`
---

# Hva er et signal form?

```
~~~graph-easy --as=boxart
[ Model ] - brukes i -> [ form() ]
[ form() ] - lager et -> [ FieldTree ]
[ FieldTree ] - har felter som er -> [ FieldTree ]
[ FieldState ] - leser og skriver -> [ FieldTree ]
~~~
```
## FieldTree

## FieldState

## [formField]
1. binder `FieldState` og `HTML`

### Eksempel

```html
<input type="slider" [formField]="matchForm.team1.score" />

```
---

# Hva er et signal form?

```
~~~graph-easy --as=boxart
[ Model ] - brukes i -> [ form() ]
[ form() ] - lager et -> [ FieldTree ]
[ FieldTree ] - har felter som er -> [ FieldTree ]
[ FieldState ] - leser og skriver -> [ FieldTree ]
~~~
```
## FieldTree

## FieldState

## [formField]
1. binder `FieldState` og `HTML`

---

# Hva er et signal form?

```
~~~graph-easy --as=boxart
[ Model ] - brukes i -> [ form() ]
[ form() ] - lager et -> [ FieldTree ]
[ FieldTree ] - har felter som er -> [ FieldTree ]
[ FieldState ] - leser og skriver -> [ FieldTree ]
[ HTML] <- bindes med\n\[fieldForm\] -> [ FieldState ]
~~~
```
## FieldTree

## FieldState

## [formField]

---

# Hva er et signal form?

```
~~~graph-easy --as=boxart
[ Model ] - brukes i -> [ form() ]
[ form() ] - lager et -> [ FieldTree ]
[ FieldTree ] - har felter som er -> [ FieldTree ]
[ FieldState ] - leser og skriver -> [ FieldTree ]
[ HTML] <- bindes med\n\[fieldForm\] -> [ FieldState ]
~~~
```
## FieldTree
Representerer modellen

## FieldState
Har nyttig informasjon om tilstand

## [formField]
binder modellen til HTML

---

```
~~~cowsay
Demoo!
~~~
```

---

# Takk for meg

Presentasjonsprogram:

https://github.com/maaslalani/slides



---
`FieldTree` distribueres ned komponenthierarkiet

```
~~~graph-easy --as=boxart
[ MatchEntry ] -> [ TeamEntry ] -> [ ScoreEntry]
[ TeamEntry ] -> [ PlayerEntry ]
~~~
```
```
~~~graph-easy --as=boxart
[ FieldTree<Match> ] -> [ FieldTree<Team> ] -> [ FieldTree<number>]
[ FieldTree<Team> ] -> [ FieldTree<string> ]
~~~
