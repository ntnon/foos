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
Når middagen er spist, nattesøvnen over, og 4 etasje i Nøstegaten 58 på ny fylles opp av kaffe, databrus, og gode folk -
---
Kommer noen til huske gårdagens triumf?
---
Neppe.
---
Men det var før.
---
Nå?
---
Nå holder vår helt egne Rain-man-Aki tellinga
---
Dine fussballoppturer og -nedturer noteres og lagres
---
Men hva om Aki skal på rutinesjekk hos tannlegen etter lunsj?
---
Da trenger man kanskje en app 
---

# Fussball app
- Lagre fussballresultater
- Heve kompetanse
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

[ Frontenden ] -> [ Signal form ]
~~~
```

---

# Hvordan lage signal form

Velkommen!

Hele frontenden bygger rundt et skjema
signal forms
for å forstå frontenden må man forstå signal forms

Hva, Hvorfor
Hvorfor Signal Forms?

---

# Hvordan lage signal form

Lag en datastruktur

```typescript
interface LoginData {
  email: string;
  password: string;
}
```
---

# Hvordan lage signal form

Lag en datastruktur

```typescript
interface LoginData {
  email: string;
  password: string;
}

```
Lag et signal

```typescript
loginModel = signal<LoginData>({
    email: '',
    password: '',
  });
```  

---

# Hvordan lage signal form

Lag en datastruktur

```typescript
interface LoginData {
  email: string;
  password: string;
}

```
Lag et signal

```typescript
loginModel = signal<LoginData>({
    email: '',
    password: '',
  });

```  

Send signalet til `form()` for å lage en `SignalForm`
```typescript
loginForm = form(this.loginModel);

```
---

# Hvordan lage signal form

Lag en datastruktur

```typescript
interface LoginData {
  email: string;
  password: string;
}

```
Lag et signal

```typescript
loginModel = signal<LoginData>({
    email: '',
    password: '',
  });

```  

Send signalet til `form()` for å lage en `SignalForm`
```typescript
loginForm = form(this.loginModel);

```

Bind til input-felter med `formField`

```HTML
<input type="email" [formField]="loginForm.email" />
<input type="password" [formField]="loginForm.password" />
```

---

# Hvordan lage signal form

Lag en datastruktur

```typescript
interface LoginData {
  email: string;
  password: string;
}

```
Lag et signal

```typescript
loginModel = signal<LoginData>({
    email: '',
    password: '',
  });

```  

Send signalet til `form()` for å lage en `SignalForm`
```typescript
loginForm = form(this.loginModel);

```

Bind til input-felter med `formField`

```HTML
<input type="email" [formField]="loginForm.email" />
<input type="password" [formField]="loginForm.password" />
```
Eller bruk direkte
```typescript
loginForm.email
loginForm.password
```

---

# Hva er et signal form?

`form(signal)` lager et `FieldTree` med samme struktur som signalet

---

# Hva er et signal form?

`form(signal)` lager et `FieldTree` med samme struktur som signalet

```typescript
  matchModel = signal<MatchFormModel>({
    team1: {offense: '', defense: ''},
    team2: {offense: '', defense: ''},
    team1GameScore: 0,
    team2GameScore: 0,
  });
   
matchForm = form(matchModel)
```    
---

# Hva er et signal form?

`form(signal)` lager et `FieldTree` med samme struktur som signalet

```typescript
  matchModel = signal<MatchFormModel>({
    team1: {offense: '', defense: ''},
    team2: {offense: '', defense: ''},
    team1GameScore: 0,
    team2GameScore: 0,
  });

matchForm = form(matchModel)

```
Hvert felt blir til et eget `FieldTree`
```typescript
matchForm                 // FieldTree
matchForm.team1           // FieldTree
matchForm.team1.offense   // FieldTree
matchForm.team1Gamescore  // FieldTree
```
---

# Hva er et signal form?

`form(signal)` lager et `FieldTree` med samme struktur som signalet

```typescript
   matchModel = signal<MatchFormModel>({
    team1: {offense: '', defense: ''},
    team2: {offense: '', defense: ''},
    team1GameScore: 0,
    team2GameScore: 0,
  });
 
matchForm = form(matchModel)

```

```typescript

FieldTree<{
  team1: FieldTree<{
    offense: FieldTree<string, any>;
    defense: FieldTree<string, any>;
  }, any>;
  team2: FieldTree<{
    offense: FieldTree<string, any>;
    defense: FieldTree<string, any>;
  }, any>;
  team1GameScore: FieldTree<number, any>;
  team2GameScore: FieldTree<number, any>;
}, any>
```
Hvert felt blir til et eget `FieldTree`
```typescript
matchForm                 // FieldTree
matchForm.team1           // FieldTree
matchForm.team1.offense   // FieldTree
matchForm.team1Gamescore  // FieldTree
```
Oppdatering av en `FieldTree` vil ikke oppdatere søsknene
```typescript
matchForm.team1.set(...)              // matchForm.team2               upåvirket
matchForm.team1.offense.set(...)      // matchForm.team1.defense       upåvirket
matchForm.team1Gamescore.set(...)     // matchForm.team2GameScore      upåvirket
matchForm.set(...)                    // alt                           påvirket

```
---

# Hva er et signal form?

`form(signal)` lager et `FieldTree` med samme struktur som signalet

```typescript
  matchModel = signal<MatchFormModel>({
    team1: {offense: '', defense: ''},
    team2: {offense: '', defense: ''},
    team1GameScore: 0,
    team2GameScore: 0,
  });
 
matchForm = form(matchModel)

```
Hvert felt blir til et eget `FieldTree`
```typescript
matchForm                 // FieldTree
matchForm.team1           // FieldTree
matchForm.team1.offense   // FieldTree
matchForm.team1Gamescore  // FieldTree
```
Oppdatering av en `FieldTree` vil ikke oppdatere søsknene
```typescript
matchForm.team1.set(...)              // matchForm.team2               upåvirket
matchForm.team1.offense.set(...)      // matchForm.team1.defense       upåvirket
matchForm.team1Gamescore.set(...)     // matchForm.team2GameScore      upåvirket
matchForm.set(...)                    // alt                           påvirket
```


komponenthierarki : signal form : datastruktur

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


```
~~~graph-easy --as=boxart
graph {
  border: 1px solid black;
  fill: oldlace;
  background: goldenrod;
  label: My sample graph;
  }
edge { label-color: green; color: blue; }

[ One ] { fill: seagreen; color: white; } -- label --> [ Two ] { shape: triangle; }
[ One ] => { arrow-style: closed; } [ Three ]
[ Five ] { fill: maroon; color: yellow; } <=> [ Three ]
[ One ] .. Test\n label ..> [ Four ]
[ Three ] { border-style: dashed; } 
.. Test\n label ..> { arrow-style: closed; } [ Six ] { label: Sixty\n Six\nand\nsix; }
[ Five ] -  Test label - > { label-color: darkslategrey; color: red; } [ Seven ]
[ Seven ] -- [ Eight ]
[ Seven ] -- [ Fuve ]
[ Five ] --> [ Eight ]
[ Five ] --> [ Seven ]
[ Four ] --> [ Fuve ]
[ Two ] -> [ Four ]
[ Three ] <-- Test label --> { arrow-style: closed; } [ Six ]
[ Eight ] .. [ None ] { shape: none; fill: red; color: brown; }
~~~
```
