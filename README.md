# DOSW - Parcial T2

**Andrés Camilo Vivas Baquero** 

**Grupo 1** 

## Herramientas de modelado

![lucid_chart.png](docs/images/lucid_chart.png)

## Herramienta de diseño

![figma.png](docs/images/figma.png)

# PARTE TEORICA


## Punto 1

### Identificacion de funcionalidades

a. Que tipo de verbo HTTP Maneja

b. Establezca si es una funcionalidad idempotente o no

c. Cuál es la razón técnica de su decisión.

d. Cuáles Roles de los identificados tienen acceso a esa funcionalidad

e. Mencione sus datos de entrada y de salida (Establezca de qué tipo
es cada propiedad y si es obligatorio o no)

f. De un ejemplo de cómo se vería la entrada y la salida.

g. Establezca qué validaciones de input y el negocio debe tener en
cuenta.

h. Establezca los códigos HTTP y mensaje para Happy Path y Flujo de
Error

- F-O1 Registro de usuario 

Verbo HTTP - POST

Idempotente - No

Razon - Cada llamada crea un nuevo recurso en base de datos. Múltiples requests con el mismo email deben retornar error, no el mismo resultado.

Roles con acceso - Cliente

Datos entrada:  

Name: "string", - Obligatorio 

Mail: "String" , - Obligatorio

password: "String" - Obligatorio

Datos Salida - Mensaje de confirmacion al usuario de creacion de perfil exitosa 

Ejemplo de entrada: 

{
"name": "Andres Vivas ",

"email": "andres.vivas-b@mail.escuelaing.edu.co ",

"password": "Contraseña123"
}

Ejemplo de salida: 

{
"id": "uuid",

"name": "Andres Vivas",

"email": "andres.vivas-b@mail.escuelaing.edu.co",

"createdAt": "2025-04-10T12:00:00Z"
}

Validaciones input - nombre no vacío, email con formato válido, contraseña mínimo 8 caracteres con al menos 1 número.

Validaciones output - el email no debe estar registrado previamente.

Codigos HTTP, mensaje happy path y flujo de error

Happy Path - 201 Created - Usuario registrado exitosamente

Email - ya existe - 409 Conflict - El correo ya está registrado

Datos inválidos - 400 Bad Request - Campo inválido

Error servidor - 500 Internal Server Error - Error interno del servidor



- F-O2 Login Usuario/ Autenticacion

Verbo HTTP - POST

Idempotente - No

Razon - Genera un token nuevo en cada llamada, aunque las credenciales sean las mismas.

Roles con acceso - Cliente / Señora cafeteria

Datos entrada:

Mail: "String" , - Obligatorio

password: "String" - Obligatorio

Datos Salida - Se genera un token y el codigo del usuario 

Ejemplo de entrada:

{

"email": "string",

"password": "string"

}

Ejemplo de salida:

{

"token": "eyJhbGciOiJIUzI1NiJ9...",

"userId": "uuid",

}

Validaciones input - email y password no vacíos, email con formato válido.

Validaciones output - las credenciales deben coincidir con un usuario registrado y activo..

Codigos HTTP, mensaje happy path y flujo de error

Happy Path	200 OK	—

Credenciales incorrectas	401 Unauthorized	Credenciales inválidas

Datos inválidos	400 Bad Request	El campo email es obligatorio


- F-O3 Los productos pueden ser consultados mediante escaneo de código QR

Verbo HTTP - GET

Idempotente - Si

Razon - GET nunca modifica estado del servidor. Múltiples llamadas iguales retornan el mismo resultado.

Roles con acceso - Cliente

Datos entrada:



Datos Salida - Autorizacion: Bearer token

Ejemplo de entrada:

{

ID-Codigo-QR: "Uuid"

}

Ejemplo de salida:

{



}


Validaciones - solo devolver productos con status disponible.

Codigos HTTP, mensaje happy path y flujo de error

Happy Path - 200 OK	— Lista de productos disponibles

Sin autenticación - 401 Unauthorized - Token requerido

Error servidor - 500 Internal Server Error - Error al consultar productos


- F-O4 Los usuarios puede crear un pedido agregando productos escaneados.

Verbo HTTP - POST

Idempotente - NO

Razon - Cada llamada puede acumular cantidades o crear nuevas entradas en el carrito.

Roles con acceso - Cliente

Datos entrada: id del producto, cantidad del producto a pedir 

Datos Salida - Autorizacion: Bearer token y informacion del pedido 

Ejemplo de entrada:

{
"productId": "uuid",

"quantity": "integer"
}


Ejemplo de salida:

{
"productId": "uuid",

"productName": "Cafe con leche",

"quantity": 2,

"unitPrice": 2500,

"subtotal": 5000
}


Validaciones - solo devolver productos con status disponible.

Codigos HTTP, mensaje happy path y flujo de error

Happy Path - 200 OK	— Lista de productos disponibles

Sin autenticación - 401 Unauthorized - Token requerido

Error servidor - 500 Internal Server Error - Error al consultar productos


- F-O5 El administrador puede cambiar el estado del pedido a:
  ○ EN_PREPARACION
  ○ ENTREGADO

Verbo HTTP - PUT

Idempotente - Si

Razon - PUT reemplaza el recurso completo. Múltiples llamadas con el mismo body producen el mismo estado.

Roles con acceso - Administrador 

Datos entrada: id del producto, cantidad del producto a pedir

Datos Salida - Autorizacion: Bearer token y informacion del pedido

Ejemplo de entrada:

{
"cambioEstadoProducto" : "Disponible"
}

Ejemplo de salida:

{

"productId": "Disponible"

}


Validaciones - Revisar si un producto en estado de disponible, no se pueda cambiar a su mismo estado actual


- F-O6 El cliente puede cancelar el pedido solo en estado CREADO.

- Verbo HTTP - PUT

Idempotente - SI

Razon - PUT reemplaza el recurso completo. Múltiples llamadas con el mismo body producen el mismo estado.

Roles con acceso - Cliente

Datos entrada: Estado del pedido 

Datos Salida - Mensaje de confirmacion de cancelacion de pedido 

Ejemplo de entrada:

{
"estadoPedido": "CREADO"
}


Ejemplo de salida:

{

}


Validaciones - solo cancelar pedido si su estado es CREADO 

