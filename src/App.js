import React, { Component } from 'react';
import logo from './logo.svg';
import './App.css';
import { withAuthenticator, AmplifySignOut } from '@aws-amplify/ui-react'
import { Amplify, API, graphqlOperation } from 'aws-amplify';
import aws_exports from './aws-exports';
import { getTodo, listTodos } from './graphql/queries'
import { createTodo, updateTodo, deleteTodo } from './graphql/mutations'
Amplify.configure(aws_exports);

class App extends Component {

  render() {

    API.graphql(createTodo, {
      input: {
        name: 'My first todo!'
      }
    })
      .then(value => console.log(value))
      .catch(err => { console.log(err) });

    API.graphql(listTodos)
      .then(value => console.log(value))
      .catch(err => { console.log(err) });

    return (
      <div className="App">
        <AmplifySignOut />
        <header className="App-header">
          <img src={logo} className="App-logo" alt="logo" />
          <p>
            Edit <code>src/App.js</code> and save to reload.
          </p>
          <a
            className="App-link"
            href="https://reactjs.org"
            target="_blank"
            rel="noopener noreferrer"
          >
            Learn React
          </a>
        </header>
      </div>
    );
  }
}


export default withAuthenticator(App);
