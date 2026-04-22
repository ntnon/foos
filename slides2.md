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

[ Frontenden ] -> [ Signal forms ]
~~~
```
For å forstå frontenden holder det å forstå forholdet mellom tre ting:
- `FieldTree`
- `FieldState`
- `[formField]`

Først lager vi et skjema

---

# Start med data

Et skjema starter med en datastruktur, for eksempel:

```typescript
interface LoginData {
  email: string;
  password: string;
}
```

---

# Legg dataene i et signal

```typescript
interface LoginData {
  email: string;
  password: string;
}

loginModel = signal<LoginData>({
  email: '',
  password: '',
});
```

loginModel -> single source of truth

---

# Lag et skjema av signalet

```typescript
loginForm = form(loginModel);
```

`form()` returnerer et `FieldTree`

---

# Samme struktur inn, samme struktur ut

1. `FieldTree` er en node som beskriver et tre eller et subtre
1. `FieldTree` speiler modellen du sender inn
1. Hvis modellen er nøstet, blir `FieldTree` også nøstet

---

# Samme struktur inn, samme struktur ut

1. `FieldTree` er en node som beskriver et tre eller et subtre
1. `FieldTree` speiler modellen du sender inn
1. Hvis modellen er nøstet, blir `FieldTree` også nøstet

```typescript
matchModel = signal({
  team1: {offense: '', defense: ''},
  team2: {offense: '', defense: ''},
  team1GameScore: 0,
  team2GameScore: 0,
});

matchForm = form(matchModel);
```

---

# Samme struktur inn, samme struktur ut

1. `FieldTree` er en node som beskriver et tre eller et subtre
1. `FieldTree` speiler modellen du sender inn
1. Hvis modellen er nøstet, blir `FieldTree` også nøstet

```typescript
matchModel = signal({
  team1: {offense: '', defense: ''},
  team2: {offense: '', defense: ''},
  team1GameScore: 0,
  team2GameScore: 0,
});

matchForm = form(matchModel);

matchForm                   // FieldTree
matchForm.team1             // FieldTree
matchForm.team1.offense     // FieldTree
matchForm.team1.defense     // FieldTree
matchForm.team1GameScore    // FieldTree
matchForm.team2GameScore    // FieldTree
```

---

# Tre konsepter

- `FieldTree` er en node i skjematreet
- `FieldState` er tilstanden til en `FieldTree`-node
- `[formField]` kobler `FieldTree` til et inputfelt

```typescript
loginForm.email                                  // FieldTree for email
loginForm.email()                                // field state for email
loginForm.email().value()                        // verdien
loginForm.email().value.set('ada@example.com');  // verdien endres i `FieldState` med value.set()
```

---

# Oppdatering via field state

```typescript
loginForm.email().value.set('ada@example.com');
loginForm.password().value.set('secret');
```

---

# Det viktigste å ta med seg

- modellen ligger i et signal
- `form(modelSignal)` lager et `FieldTree`
- `FieldTree` speiler modellen
- `[formField]` kobler felt til UI
- `field()` gir state for feltet
- `field().value.set(...)` oppdaterer verdien


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
