import React, { Component } from 'react';
import logo from './logo.svg';
import './App.css';
import { withAuthenticator, AmplifySignOut } from '@aws-amplify/ui-react'
import { Amplify, API, graphqlOperation } from 'aws-amplify';
import aws_exports from './aws-exports';
import { getTodo, listTodos, recfunction } from './graphql/queries'
import { createTodo, updateTodo, deleteTodo } from './graphql/mutations'
Amplify.configure(aws_exports);

class App extends Component {

  render() {

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

  async componentDidMount() {
    if (false) {
      const createdTodo = await API.graphql(graphqlOperation(createTodo, {
        input: {
          name: 'My first todo!'
        }
      }));

      console.log(createdTodo)
    }
    if (false) {
      const todoList = await API.graphql(graphqlOperation(listTodos));

      console.log(todoList)
    }
    if (true) {
      const recfunctionString = await API.graphql(graphqlOperation(recfunction, {msg: "test"}));

      console.log(recfunctionString)
    }
  }
}


export default withAuthenticator(App);
